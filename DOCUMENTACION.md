# Danganime — Documentación técnica

Documentación completa de la arquitectura, el sistema de bloqueo de anuncios y el
proceso de compilación/publicación de **Danganime**.

---

## Índice

1. [Visión general](#1-visión-general)
2. [Arquitectura](#2-arquitectura)
3. [El navegador nativo](#3-el-navegador-nativo)
4. [Sistema de bloqueo de anuncios](#4-sistema-de-bloqueo-de-anuncios)
5. [Configuración de sitios](#5-configuración-de-sitios)
6. [Listas de filtros](#6-listas-de-filtros)
7. [Compilación y publicación](#7-compilación-y-publicación)
8. [Pruebas en dispositivo](#8-pruebas-en-dispositivo)
9. [Estructura del repositorio](#9-estructura-del-repositorio)
10. [Seguridad](#10-seguridad)
11. [Limitaciones conocidas](#11-limitaciones-conocidas)

---

## 1. Visión general

Danganime es una app Android híbrida construida con **Capacitor 6**:

- El **frontend** (menú, categorías, selector de sitios) es HTML/CSS/JS (`www/`).
- Los **sitios** se abren en un **WebView nativo** propio (no en un iframe) para poder
  aplicar el bloqueo de anuncios sobre la página real.
- El **bloqueo de anuncios** usa las listas de **uBlock Origin**.

| Componente | Tecnología |
|---|---|
| Frontend | HTML5 · CSS3 · JavaScript (vanilla) |
| Contenedor | Capacitor 6 |
| Nativo | Java (Android SDK) |
| min / target SDK | 22 (Android 5.1) / 34 (Android 14) |
| Filtros | EasyList · EasyPrivacy · uBlock Origin |
| CI/CD | GitHub Actions |

---

## 2. Arquitectura

```
┌───────────────────────────────────────────────────────────┐
│                    APLICACIÓN ANDROID                     │
│                                                           │
│  WebView de Capacitor (interfaz)                          │
│    www/index.html · css/style.css · js/app.js             │
│         │  (toca un sitio)                                │
│         ▼  danganime://open?url=…&title=…                 │
│  MainActivity.java                                        │
│    ├─ AppSchemeClient  (intercepta el esquema propio)     │
│    └─ WebView overlay (sitio TOP-LEVEL)                   │
│         ├─ AdBlockWebViewClient                           │
│         │    ├─ shouldInterceptRequest  → bloqueo de red  │
│         │    ├─ onPageFinished          → CSS + JS        │
│         │    └─ shouldOverrideUrlLoading→ anti-popunder   │
│         └─ BrowserChromeClient (fullscreen de vídeo)      │
└───────────────────────────────────────────────────────────┘
```

**Flujo al abrir un sitio:**

1. El usuario toca un sitio en el menú.
2. `app.js` hace `window.location = 'danganime://open?url=…'`.
3. `AppSchemeClient` intercepta el esquema y llama a `openBrowser(url, title)`.
4. Se muestra un **WebView nativo** a pantalla completa que carga el sitio como
   documento principal.
5. Se aplican las capas de bloqueo sobre la página real.

---

## 3. El navegador nativo

El sitio se carga **top-level** (no en iframe) por dos motivos:

1. Muchos sitios envían `X-Frame-Options` / CSP `frame-ancestors` y no se pueden embeber.
2. Solo inyectando CSS/JS en el **documento principal** funciona el bloqueo cosmético.

**Barra de herramientas nativa:**

| Botón | Acción |
|---|---|
| 🏠 Inicio | Cierra el navegador y vuelve al menú de la app |
| ↻ Recargar | Recarga la página |
| ⛶ Pantalla completa | Oculta/muestra la barra |
| ↗ Abrir externo | Abre la URL en el navegador del sistema |
| × Cerrar | Cierra y vuelve al selector de sitios |

El botón **atrás** del sistema navega por el historial del sitio y, si no hay más,
cierra el navegador.

---

## 4. Sistema de bloqueo de anuncios

### Capa 1 — Red (Java)

`AdBlockWebViewClient.shouldInterceptRequest()` compara el **host** de cada petición
contra ~98.700 dominios (EasyList + EasyPrivacy + uBlock Origin), con **coincidencia
exacta y por sufijo** (no substrings, para evitar falsos positivos). Si coincide,
devuelve una respuesta vacía.

- `SAFE_BASES` excluye CDNs y reproductores legítimos (JW Player, Streamtape, VK,
  Vimeo, Cloudflare, Google APIs, etc.) para no romper vídeos ni librerías.
- La lista se carga en **segundo plano** (hilo `filters-loader`) para no bloquear la UI.

### Capa 2 — Cosmético (CSS)

Se inyecta un `<style>` (codificado en **Base64**) con selectores que ocultan banners,
overlays, popups, avisos anti-adblock y las marcas de anuncios de reproductores.

### Capa 3 — JavaScript

Un script por página:

- bloquea `fetch()` y `XMLHttpRequest` hacia hosts de anuncios,
- elimina nodos (`SCRIPT`/`IFRAME`/`IMG`) de redes de anuncios conocidas,
- bloquea `window.open` y clics hacia hosts de anuncios.

### Capa 4 — Anti-popunder / anti-redirección

`shouldOverrideUrlLoading` cancela la navegación top-level hacia dominios de terceros
que no sean de reproductor/sitios permitidos, evitando popunders al pulsar el
reproductor.

### Ajustes específicos por sitio

- **Runtime**: fuerza el **volumen al 100%** (el reproductor no tiene botón de volumen)
  y oculta el aviso "Install the Runtime app".
- **Pluto TV**: oculta el reproductor persistente de la portada, permite el scroll en
  la portada y auto-oculta la barra de reproducción a los 8 s (reaparece al tocar).

---

## 5. Configuración de sitios

Los sitios se definen en `www/js/app.js`:

```js
const SITES = {
  anime: {
    title: 'Ver Anime',
    badge: 'badge-anime',
    sites: [
      { name: 'JKAnime', url: 'https://jkanime.net/', desc: '…', favicon: 'favicons/jkanime.png' },
      // …
    ]
  },
  // peliculas, manhwa, manga, novelas…
};
```

**Categorías:** Ver Anime · Películas · Manhwas · Mangas · Novelas Ligeras.

Para añadir un sitio: agrega la entrada y coloca su favicon (64×64 PNG) en
`www/favicons/`. Si es un reproductor legítimo, añade su dominio a `SAFE_BASES`.

---

## 6. Listas de filtros

Viven en `android/app/src/main/assets/filters/`:

| Archivo | Contenido |
|---|---|
| `blocked_domains.txt` | ~98.700 dominios de anuncios/trackers |
| `cosmetic_selectors.txt` | Selectores CSS para ocultar anuncios |

Se regeneran con [`tools/update_filters.py`](tools/update_filters.py), que descarga las
listas de uBlock Origin, extrae los dominios, excluye `SAFE_BASES` y escribe el asset.

```bash
python3 tools/update_filters.py
```

---

## 7. Compilación y publicación

**Requisitos:** Node.js 18+, JDK 17, Android SDK (compileSdk 34).

```bash
npm install
npx cap sync android
cd android && ./gradlew assembleDebug
# APK: android/app/build/outputs/apk/debug/app-debug.apk
```

**CI (GitHub Actions):** al empujar un tag `v*`, el workflow
[`.github/workflows/build.yml`](.github/workflows/build.yml) compila el APK y crea una
**Release** con el APK adjunto.

```bash
git tag v1.9.3 && git push origin v1.9.3
```

---

## 8. Pruebas en dispositivo

La app se puede controlar por **ADB + Chrome DevTools Protocol (CDP)** para pruebas
automatizadas sin tocar la pantalla:

```bash
adb install -r apks/Danganime-v1.9.3.apk
PID=$(adb shell pidof com.danganime.app)
adb forward tcp:9222 localabstract:webview_devtools_remote_$PID
curl -s http://127.0.0.1:9222/json   # lista de targets del WebView
```

Con CDP se inspecciona el DOM de los sitios, se comprueba el bloqueo de anuncios y se
verifica la reproducción de vídeo.

```bash
adb logcat -s Danganime   # log del bloqueador
```

---

## 9. Estructura del repositorio

```
Danganime/
├── www/                    Frontend (HTML/CSS/JS)
├── android/                Proyecto Android (Capacitor)
│   └── app/src/main/
│       ├── java/com/danganime/app/MainActivity.java
│       └── assets/filters/  Listas de bloqueo
├── docs/                   Documentación por temas
├── logs/                   Changelog por versión
├── apks/                   APKs publicados
├── screenshots/            Capturas y demo (GIF)
├── tools/                  Scripts (regenerar filtros)
├── DOCUMENTACION.md        Este documento
├── README.md · CHANGELOG.md · LICENSE · CONTRIBUTING.md · SECURITY.md
└── .github/                Workflows e issue templates
```

---

## 10. Seguridad

- `android:allowBackup="false"` (no se respaldan cookies/historial del WebView).
- Sin `addJavascriptInterface` expuesto.
- Acceso a archivos locales restringido.
- Comunicación app ↔ nativo por esquema propio (`danganime://`), sin reemplazar el
  `WebViewClient` de Capacitor.
- Sin claves ni tokens en el repositorio.

---

## 11. Limitaciones conocidas

- **SSAI**: algunos servicios (p. ej. Pluto TV) insertan los anuncios **dentro del propio
  stream**, por lo que no se pueden eliminar desde el cliente sin romper la reproducción.
  Se mitiga ocultando marcas y mejorando la experiencia (barra auto-ocultable).
- Los anuncios **internos** de reproductores de terceros (iframes cross-origin) no se
  pueden ocultar por CSS; se bloquean por red cuando usan dominios de anuncios.
- El bloqueo depende de las listas; se recomienda regenerarlas periódicamente.
