# Arquitectura del Proyecto

## Visión General

Danganime utiliza una arquitectura **híbrida** que combina:

1. **Frontend Web** (HTML/CSS/JS) - Interfaz de usuario y lógica de navegación
2. **Contenedor Nativo** (Capacitor/Android) - Funcionalidades nativas del dispositivo
3. **WebView Nativo de Sitios** (overlay) - Carga los sitios web como **documento principal** con bloqueo de anuncios real

> **Importante (desde v1.3.0):** los sitios **ya no** se cargan dentro de `<iframe>` de la app.
> Un iframe cross-origin impedía inyectar CSS/JS ad-blocker y muchos sitios lo bloquean
> (`X-Frame-Options`, CSP `frame-ancestors`, frame-busting). Ahora cada sitio se abre en un
> **WebView Android nativo a pantalla completa** sobre la interfaz, cargado como página principal.

```
┌─────────────────────────────────────────────────────────────┐
│                    APLICACIÓN ANDROID                        │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐   │
│  │        WEBVIEW CAPACITOR (interfaz de la app)        │   │
│  │   Splash / Menú / Selector de sitios (HTML/CSS/JS)  │   │
│  └─────────────────────────────────────────────────────┘   │
│                           │                                  │
│        toca un sitio → 'danganime://open?url=...'           │
│                           ▼                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │   WEBVIEW NATIVO OVERLAY (MainActivity)             │   │
│  │   ├─ Toolbar nativa (← ↻ ⛶ ↗ ×)                     │   │
│  │   └─ Sitio web cargado TOP-LEVEL                     │   │
│  │       └─ AdBlock: red + CSS + JS sobre la página     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         MAINACTIVITY (JAVA ANDROID)                  │   │
│  │  AppSchemeClient · BrowserAdClient · Fullscreen     │   │
│  │  Immersive Mode · Carga de filtros uBlock (assets)  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Capas de la Aplicación

### 1. Capa de Presentación (Frontend)

**Archivos**: `www/index.html`, `www/css/style.css`, `www/js/app.js`

Responsable de:
- Renderizar la interfaz de usuario
- Manejar animaciones y transiciones
- Navegación entre pantallas
- Reproducir efectos de sonido
- Lanzar los sitios pidiendo al nativo abrirlos (esquema `danganime://`)

### 2. Capa Nativa (Android)

**Archivo**: `android/app/src/main/java/com/danganime/app/MainActivity.java`

Responsable de:
- Cargar los sitios web **top-level** en un WebView nativo dedicado (overlay)
- Bloqueo de anuncios a nivel de red (host-matching)
- Inyección de CSS cosmético y JS en la página real del sitio
- Control de pantalla completa y modo inmersivo
- Configuración del WebView de Capacitor (esquema `danganime://`)
- Carga de listas de filtros (uBlock) desde `assets/filters/`

## Flujo de Datos

```
Usuario toca una categoría
        │
        ▼
┌─────────────────┐
│  app.js         │
│  openCategory() │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Usuario toca   │
│  un sitio       │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  app.js  openSite()                │
│  window.location = 'danganime://   │
│  open?url=<url>&title=<name>'      │
└───────────────┬─────────────────────┘
                ▼
┌─────────────────────────────────────┐
│  MainActivity  AppSchemeClient      │
│  intercepta 'danganime://'          │
└───────────────┬─────────────────────┘
                ▼
┌─────────────────────────────────────┐
│  WebView nativo overlay carga el    │
│  sitio TOP-LEVEL + AdBlock (red,    │
│  CSS cosmético, JS)                 │
└─────────────────────────────────────┘
```

## Decisiones de Diseño

### ¿Por qué WebView nativo y no iframe?

1. **Sitios no bloqueables**: el iframe era bloqueado por `X-Frame-Options`/CSP y rompía la navegación.
2. **Ad-block real**: solo inyectando CSS/JS sobre el **documento principal** del sitio funciona el bloqueo cosmético.
3. **Control total**: `shouldInterceptRequest` se aplica al sitio real.
4. **UX**: back del sistema navega el historial del sitio; cerrar vuelve al menú sin recargar la app.

### ¿Por qué comunicación por esquema `danganime://`?

En lugar de reemplazar el WebViewClient de Capacitor (lección v1.2.2), se extiende `BridgeWebViewClient`
y solo se intercepta el esquema propio antes de delegar al `super`, sin romper la carga de archivos locales.
