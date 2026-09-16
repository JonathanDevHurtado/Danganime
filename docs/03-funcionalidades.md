# Funcionalidades

## 1. Splash Screen

**Descripción**: Pantalla de bienvenida que se muestra al abrir la app.

**Características**:
- Logo animado con efecto bounce
- Título "Danganime" con degradado brillante
- Subtítulo con las categorías disponibles
- Sonido de bienvenida reproducido una vez
- Transición suave al menú principal

**Duración**: 2.2 segundos

**Archivos involucrados**:
- `www/index.html` (líneas 14-20)
- `www/css/style.css` (líneas 33-93)
- `www/js/app.js` (función `init()`)
- `www/js/sounds.js` (función `welcome()`)

---

## 2. Menú Principal

**Descripción**: Pantalla con las 5 categorías de contenido.

**Categorías**:
| Categoría | Icono | Color | Sitios |
|-----------|-------|-------|--------|
| Ver Anime | 🎬 | Rosa | 4 |
| Películas | 🎥 | Verde | 3 |
| Manhwas | 📖 | Morado | 3 |
| Mangas | 📚 | Cian | 3 |
| Novelas Ligeras | 📕 | Naranja | 3 |

**Características**:
- Tarjetas con animación bounce escalonada
- Borde lateral de color por categoría
- Efecto de rebote al tocar
- Sonido de navegación

**Archivos involucrados**:
- `www/index.html` (líneas 23-55)
- `www/css/style.css` (líneas 123-275)

---

## 3. Selector de Sitios

**Descripción**: Lista de sitios web disponibles para una categoría.

**Características**:
- Favicon de cada sitio (cargado desde Google)
- Nombre del sitio
- URL del sitio
- Badge con descripción
- Animación de entrada deslizante

**Archivos involucrados**:
- `www/index.html` (líneas 57-69)
- `www/js/app.js` (función `openCategory()`)

---

## 4. Visor de Sitios Web (Navegador Nativo)

**Descripción**: Al tocar un sitio, se abre un **WebView Android nativo de pantalla completa**
(overlay) que carga la página como **documento principal**. Sustituye al antiguo iframe (v1.3.0).

**Características**:
- Barra de herramientas nativa (Android):
  - ← retroceder en el historial
  - ↻ recargar
  - ⛶ ocultar/mostrar la barra (modo video/inmersivo)
  - ↗ abrir la URL en el navegador externo
  - × cerrar y volver al menú de la app
- Título del sitio actualizado automáticamente
- Fondo negro para mejor experiencia de video
- Video en pantalla completa nativo (WebChromeClient)
- Bloqueo de anuncios integrado (red + CSS + JS) sobre la página real

**Archivos involucrados**:
- `android/.../MainActivity.java` (overlay, toolbar, WebView)
- `www/js/app.js` (función `openSite()` → esquema `danganime://open`)

---

## 5. Bloqueo de Anuncios

**Descripción**: Sistema multicapa aplicado sobre el documento principal del sitio.

**Capas**:
1. **Red** (Android): `shouldInterceptRequest` bloquea 177 dominios por host exacto/sufijo
2. **CSS** (Base64): oculta 52 selectores de contenedores de anuncios
3. **JavaScript**: bloquea `fetch`/`XHR` y elimina iframes/scripts de anuncios insertados dinámicamente

**Archivos involucrados**:
- `android/.../MainActivity.java`
- `android/app/src/main/assets/filters/` (listas: `curated_domains.txt`, `cosmetic_selectors.txt`)

Más detalle en [04-bloqueo-anuncios.md](./04-bloqueo-anuncios.md).

---

## 6. Modo Fullscreen

**Descripción**: Modo inmersivo para ver contenido sin distracciones.

**Características**:
- Oculta barra de estado y de navegación del sistema
- Botón ⛶ en la barra nativa para ocultar/mostrar la barra del navegador
- Soporte para fullscreen nativo de video
- Pantalla no se apaga (FLAG_KEEP_SCREEN_ON)

**Archivos involucrados**:
- `android/.../MainActivity.java` (método `hideSystemUI()`, `BrowserChromeClient`)

---

## 7. Efectos de Sonido

**Descripción**: Sonidos generados con Web Audio API.

**Sonidos disponibles**:
| Sonido | Uso | Frecuencia |
|--------|-----|------------|
| click | Selección | 800Hz |
| tap | Botones | 600Hz |
| open | Abrir categoría | 500-900Hz |
| back | Regresar | 400-600Hz |
| splash | Inicio | 300-900Hz |
| welcome | Bienvenida | Archivo M4A (BienvenidaDanganime) |

**Archivos involucrados**:
- `www/js/sounds.js`

---

## 8. Navegación

**Descripción**: Sistema de navegación entre pantallas.

**Estados de la interfaz**:
```
splash → menu → sites
              ↑     │ (tocar sitio)
              └─────┘
                    ▼
          Navegador NATIVO (overlay)
          ←/× cierra → vuelve a 'sites'
```

**Retroceso del sistema (Android)**:
- Navegador abierto: retrocede en el historial del sitio; sin más historial, lo cierra.
- Navegador cerrado: JS `handleBackPress()` navega `sites → menu`, o sale de la app en el menú.

**Archivos involucrados**:
- `www/js/app.js` (variable `currentView`, función `handleBackPress()`)
- `android/.../MainActivity.java` (`onBackPressed()`)
