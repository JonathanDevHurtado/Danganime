# Modo Fullscreen y Experiencia de Video

## Visión General

Danganime está diseñado para ofrecer la mejor experiencia al ver anime, películas y series. El sistema incluye modo inmersivo, soporte para fullscreen nativo y optimizaciones de pantalla.

---

## Modo Inmersivo

**Descripción**: Oculta automáticamente las barras del sistema al abrir la app.

### Implementación Android

**API 30+ (Android 11+)**:
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    window.setDecorFitsSystemWindows(false);
    window.getInsetsController().hide(
        WindowInsets.Type.statusBars() |
        WindowInsets.Type.navigationBars()
    );
    window.getInsetsController().setSystemBarsBehavior(
        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    );
}
```

**API anterior**:
```java
window.getDecorView().setSystemUiVisibility(
    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    | View.SYSTEM_UI_FLAG_FULLSCREEN
    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
);
```

### Comportamiento

| Estado | Barras | Acción del usuario |
|--------|--------|-------------------|
| Normal | Ocultas | Swipe desde borde muestra barras temporalmente |
| Fullscreen | Ocultas | Botón ⛶ activa fullscreen nativo |
| Video | Ocultas | Se mantiene en fullscreen |

---

## Fullscreen Nativo de Video

**Descripción**: Soporte para cuando el sitio web entra en fullscreen (botón de video).

### WebChromeClient Personalizado (navegador nativo)

```java
private class BrowserChromeClient extends WebChromeClient {
    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        // Agregar la vista del video al overlay del navegador (encima de todo)
        browserToolbar.setVisibility(View.GONE);   // ocultar barra
        browserOverlay.addView(view, new FrameLayout.LayoutParams(
            MATCH_PARENT, MATCH_PARENT
        ));
        hideSystemUI();
    }

    @Override
    public void onHideCustomView() {
        // Remover la vista del video y restaurar la barra
        browserOverlay.removeView(customView);
        customView = null;
        browserToolbar.setVisibility(View.VISIBLE);
    }
}
```

---

## Botón Fullscreen (⛶)

**Descripción**: Botón de la **barra de herramientas nativa** del navegador que oculta/muestra la barra.

### Funcionamiento

```java
browserToolbar.addView(toolbarButton("⛶", v -> {
    boolean hidden = browserToolbar.getVisibility() == View.GONE;
    browserToolbar.setVisibility(hidden ? View.VISIBLE : View.GONE);
}, "Pantalla completa"));
```

### Flujo

```
Usuario toca ⛶ (barra nativa del navegador)
        │
        ▼
┌───────────────────────┐
│ Oculta la barra del   │
│ navegador nativo      │
└───────────┬───────────┘
            ▼
┌───────────────────────┐
│ Solo contenido:       │
│ página/sitio a pantalla completa
└───────────────────────┘
```

---

## Optimizaciones de Pantalla

### 1. Viewport Configuración

```html
<meta name="viewport" content="
    width=device-width, 
    initial-scale=1.0, 
    maximum-scale=1.0, 
    user-scalable=no,
    viewport-fit=cover
">
```

- `viewport-fit=cover`: Extiende el contenido a las áreas de recorte

### 2. Safe Areas (CSS)

La interfaz de la app respeta las safe areas en sus cabeceras
(`padding-top: max(8px, env(safe-area-inset-top))`). El navegador nativo es de pantalla completa.

### 3. WebSettings

```java
settings.setLoadWithOverviewMode(true);  // Ajustar al ancho
settings.setUseWideViewPort(true);       // Usar viewport completo
settings.setMediaPlaybackRequiresUserGesture(false);  // Auto-play
```

---

## Flags de Mantenimiento

### FLAG_KEEP_SCREEN_ON

```java
window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
```

**Efecto**: La pantalla no se apaga mientras se usa la app.

---

## Compatibilidad

| Sitio | Fullscreen de video | Auto-play |
|-----------------|------------|-----------|
| AnimeAV1 | ✅ | ✅ |
| JKAnime | ✅ | ✅ |
| AnimeOnlineNinja | ✅ | ✅ |
| ManhwaWeb | ✅ | ✅ |
| Peelink | ✅ | ✅ |

### Al abrir un sitio:
1. Overlay nativo con fondo negro y título del sitio
2. Carga del sitio sin barras del sistema (inmersivo)
3. Soporte para fullscreen del video

### Al tocar ⛶:
1. Barra nativa del navegador se oculta
2. Solo queda el contenido a pantalla completa

### Al tocar de nuevo:
1. La barra nativa reaparece

### Al tocar × o atrás del sistema:
1. Se cierra el navegador nativo
2. Vuelta al selector de sitios de la app (sin recargar)

## Compatibilidad

| Navegador/Sitio | Fullscreen | PiP | Auto-play |
|-----------------|------------|-----|-----------|
| Chrome Android | ✅ | ✅ | ✅ |
| Samsung Internet | ✅ | ✅ | ✅ |
| Firefox Android | ✅ | ⚠️ | ✅ |
| AnimeAV1 | ✅ | ✅ | ✅ |
| JKAnime | ✅ | ✅ | ✅ |
