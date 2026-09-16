# Sistema de Bloqueo de Anuncios

## Visión General

Danganime implementa un sistema de bloqueo de anuncios tipo uBlock Origin aplicado sobre el
**documento principal de cada sitio** (el sitio se abre en un WebView nativo, no en un iframe).
Esto es clave: solo inyectando CSS/JS en la propia página del sitio funciona el bloqueo cosmético.

---

## Arquitectura (desde v1.3.0)

```
WebView nativo overlay (sitio TOP-LEVEL)
        │
        ├─ shouldInterceptRequest()  → bloqueo de RED (retorna respuesta vacía)
        │
        ├─ onPageStarted()  → inyección CSS cosmético (Base64)
        │
        └─ onPageFinished() → inyección CSS cosmético + JS (fetch/XHR/MutationObserver)
```

Los filtros se cargan desde `android/app/src/main/assets/filters/`:
- `blocked_domains.txt` → **~98.700 dominios** de redes publicitarias/trackers (derivados de las listas de uBlock: EasyList, EasyPrivacy, etc.), tras excluir subtrees de infraestructura/CDN (`SAFE_BASES`).
- `cosmetic_selectors.txt` → regla CSS con **95 selectores** para ocultar contenedores de anuncios (incluye redes reales detectadas: AdAngle, popunders, etc.).

---

## Capa 1: Bloqueo a Nivel de Red (Java)

**Método**: `shouldInterceptRequest()` en `AdBlockWebViewClient` (WebView del navegador nativo).

```java
if (isAdUrl(url)) return blockRequest(); // respuesta vacía (bloquea la descarga)
return null;                             // deja cargar lo demás
```

**Matching por HOST (NO substring)** — evita falsos positivos:

```java
private boolean isAdUrl(String url) {
    String host = extractHost(url);
    if (host == null) return false;
    for (String d : BLOCKED_DOMAINS) {
        if (host.equals(d) || host.endsWith("." + d)) return true;
    }
    return false;
}
```

> Antes se usaba `lowerUrl.contains(domain)`, que bloqueaba sitios legítimos
> (p. ej. `turn.com` bloqueaba `return.com`). **Eliminado.**
> También se eliminó el bloqueo por keywords de URL (`/ads/`, `/banner/`…), propenso a falsos positivos.

---

## Capa 2: Inyección CSS cosmético

**Método**: `injectCosmeticCss()`

El CSS se envía **codificado en Base64** y se decodifica con `atob()` en la página:

```java
String b64 = Base64.encodeToString(COSMETIC_CSS.getBytes(), Base64.NO_WRAP);
String js = "(function(){if(!document.getElementById('danganime-adblock')){"
    + "var s=document.createElement('style');s.id='danganime-adblock';"
    + "s.appendChild(document.createTextNode(atob('" + b64 + "')));"
    + "document.head.appendChild(s);}})();";
view.evaluateJavascript(js, null);
```

> Antes se escapaba con `String.replace("'", "\\'")`, que solo reemplazaba la primera comilla,
> generando JS inválido → la inyección fallaba en silencio. **Base64 elimina el problema.**

**Selectores ocultados** (extracto):
```css
.adsbygoogle, ins.adsbygoogle,
[id*='google_ads'], [class*='google-ad'],
[class*='ad-wrapper'], [class*='ad-banner'],
[class*='ad-popup'], [class*='ad-overlay'], [class*='ad-modal'],
[class*='ad-interstitial'], [class*='ad-sticky'],
div[class*='gpt-ad'], div[id*='gpt-ad'],
div[class*='video-ads'], div[class*='player-ad'],
iframe[src*='doubleclick'], iframe[src*='googlesyndication'],
iframe[width='300'][height='250'], iframe[width='728'][height='90'],
div[class*='preroll'], div[class*='midroll'], div[class*='postroll']
```
**Propiedades aplicadas**:
```css
{
    display:none!important; visibility:hidden!important; height:0!important;
    max-height:0!important; overflow:hidden!important; opacity:0!important;
    pointer-events:none!important; z-index:-1!important;
    position:absolute!important; left:-9999px!important; top:-9999px!important;
}
```

---

## Capa 3: Inyección JavaScript

**Método**: `injectAdBlockJs()` — construido con los mismos 177 dominios (serializados a JSON).

Funciones dentro de la página del sitio:
1. `isad(url)` → compara el **host** (no substring) contra la lista.
2. Bloquea `fetch()` y `XMLHttpRequest` hacia hosts de anuncios.
3. Elimina nodos insertados dinámicamente cuyo `id`/clase contenga un **token de red** conocido (`adangle`, `popunder`, `adsterra`, …).
4. `window.open` y clics en enlaces hacia hosts de anuncios se cancelan.
5. La ocultación cosmética deja de hacerse por JS (evita falsos positivos); la hace el CSS de la Capa 2.

Además, en `shouldOverrideUrlLoading` (Java) se **cancela la navegación top-level hacia dominios de anuncios**, evitando popups/secuestro al pulsar reproducción.

---

## Estadísticas

| Métrica | Valor |
|---------|-------|
| Dominios bloqueados (red) | ~98.700 (host exacto con recorte de subdominios) |
| Excluidos por infraestructura (`SAFE_BASES`) | CDNs, reproductores (JW Player, Streamtape, VK, Vimeo…) y megaservicios |
| Carga de filtros | En segundo plano, ~1.4 s (pre-filtrado en build-time) |
| Dominios usados en JS | ~53 (los más activos) |
| Selectores CSS | 95 |
| Inyección CSS | Base64 (sin errores de escaping) |
| Ámbito | Documento principal del sitio (top-level) |
| Impacto en rendimiento | Mínimo (lookup HashSet O(1) por host) |

---

## Limitaciones

1. **Bloqueo de red** solo aplica a subrecursos y subframes, no al documento principal.
2. **Elementos dinámicos** ocultados por CSS persisten aunque cambie el DOM; la Capa 3 elimina iframes/scripts nuevos.
3. **Evasión**: algunos sitios rotan dominios o usan proxys; requiere actualizar las listas.

## Buenas prácticas aplicadas

- Comparar por host exacto/sufijo (nunca `contains` con substrings cortos).
- No bloquear por keywords de URLs (falsos positivos).
- No interceptar el documento principal del sitio.
- Inyectar CSS vía Base64 para evitar problemas de escaping.
- Aplicar el bloqueador solo al WebView del navegador nativo, no al WebView de Capacitor (no romper la app).
