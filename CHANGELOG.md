# Changelog

Todos los cambios importantes de **Danganime**.
El detalle por versión está en [`logs/`](logs/).

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el versionado [SemVer](https://semver.org/lang/es/).

---

## [1.9.3] — 2026-09-16

### Cambiado
- **Pluto TV**: las marcas de anuncios de la barra vuelven a mostrarse (avisan al usuario).
- La barra de reproducción se **oculta a los 8 s** y **reaparece al tocar** el video.

## [1.9.2] — 2026-09-16

### Añadido
- **Pluto TV**: se ocultan las marcas de anuncios de la barra (`.avia-slider-list-item`) y
  se añade auto-skip si el reproductor ofrece botón de omitir.
- Nota: los anuncios de Pluto son **SSAI** (van dentro del stream) y no se pueden eliminar
  desde la app sin romper la reproducción.

## [1.9.1] — 2026-09-16

### Corregido
- **Pluto TV**: la barra de reproducción se mostraba rota (slider en 0). Se forzó
  `avia-control-bar` con `flex-direction: column`, restaurando el slider completo en
  vertical y horizontal.

## [1.9.0] — 2026-09-16

### Corregido
- **Pluto TV (pantalla completa)**: se muestra la barra de reproducción forzando visible
  `avia-control-bar` (el reproductor la ocultaba con `display: none`).

## [1.8.9] — 2026-09-16

### Añadido
- **Pluto TV (inicio)**: scroller manual por toque para poder desplazarse (sobre todo en
  horizontal), sin modificar el layout ni romper la página.

## [1.8.8] — 2026-09-16

### Corregido
- **Pluto TV (horizontal)**: el reproductor del episodio ya no se oculta ni se pausa.
  El bloqueo del dock se aplica **solo en la portada** (`/home`); en el resto se restaura.

## [1.8.6] — 2026-09-16

### Corregido
- **Pluto TV**: el video persistente se pausa y silencia **solo** mientras está en el
  contenedor `PersistentAlwaysOnPlayer`; al salir (contenido on-demand) se **restaura** su
  `play()`, por lo que los episodios vuelven a reproducir.

## [1.8.4] — 2026-09-16

### Corregido
- **Runtime**: eliminado el aviso inferior "Install the Runtime app" (Google Play).

## [1.8.3] — 2026-09-16

### Corregido
- **Pluto TV**: el reproductor persistente ya no solo se oculta; su video queda
  **pausado y silenciado** (se anula su `play()` para que no se reanude).

## [1.8.1] — 2026-09-16

### Cambiado
- **Runtime**: restaurado el video superior (`#ottera-linear-player`); se mantiene el volumen al 100%.
- **Pluto TV**: se mantiene oculto el reproductor inferior.

## [1.8.0] — 2026-09-16

### Añadido
- **Runtime**: oculto el video de autoplay (`#ottera-linear-player`) y **volumen forzado al 100%**
  (el reproductor no tiene botón de volumen).
- **Pluto TV**: oculto el reproductor persistente inferior (`PersistentAlwaysOnPlayer`).

## [1.7.3] — 2026-09-15

### Cambiado
- **Guard anti-redirección** reforzado: cancela toda navegación top-level a dominios
  de terceros que no sean de reproductor/sitios permitidos (con o sin toque).

### Añadido
- Dominios de anuncios observados: `aphacicfable.com`, `heptacesert.cyou`.

### Verificado
- Reproducción sin anuncios en Anime (JKAnime, AnimeAV1, AnimeFenix, AnimeOnlineNinja)
  y Películas (Runtime, Pluto TV, Peelink), sin matar el reproductor.

## [1.7.2] — 2026-09-15

### Añadido
- **10 sitios nuevos** y categoría **Novelas Ligeras** (según `paginasdanganime.txt`).
- Favicons de los 10 sitios nuevos.
- Selectores para overlays de "chat" con anuncios (AnimeFenix) y avisos anti-adblock.

### Verificado
- Los 10 sitios nuevos cargan con bloqueador inyectado y **0 anuncios visibles**.

## [1.7.0] — 2026-09-15

### Añadido
- Reorganización de categorías y sitios nuevos.
- Botón **Inicio (🏠)** que vuelve al menú de la app.

## [1.6.5] — 2026-09-09

### Corregido
- Guard anti-popunder ya no rompe reproductores: permite hosts que "parecen reproductor"
  (`stream`, `vid`, `player`, `nova`, `voe`, etc.).

## [1.6.4] — 2026-09-09

### Corregido
- Desbloqueo de servidores de AnimeNinja (`earnvids`, `savefiles`, `vidara`) e iconos
  del reproductor (`imgur`, `ibb`, `gyazo`).

## [1.6.2] — 2026-09-09

### Añadido
- **Guard anti-popunder**: cancela navegaciones de script a dominios de terceros.

## [1.6.1] — 2026-09-09

### Corregido
- **ANR / lentitud al iniciar**: la carga de filtros pasa a segundo plano y se
  pre-filtra en build-time (**7.884 ms → 1.388 ms**).
- Desbloqueo de CDNs/reproductores (JW Player, Streamtape, VK, Vimeo, etc.).
- AnimeOnlineNinja: reescritura automática `www1 → ww3` (Cloudflare).

## [1.5.1] — 2026-09-09

### Corregido
- Reproductor de AnimeAV1 (JW Player estaba bloqueado por error).
- Botón Inicio → menú de la app.

## [1.5.0] — 2026-09-09

### Cambiado
- El botón "Retroceder" pasa a **Inicio (🏠)**.
- Se elimina el botón flotante de recuperación de fullscreen.

## [1.4.0] — 2026-09-09

### Añadido
- Nuevo **audio de bienvenida** (`BienvenidaDanganime.m4a`).
- **Manga Million** se abre dentro de la app.

### Corregido
- Slot de anuncios **Galaksion** en el lector de ManhwaWeb.

## [1.3.1] — 2026-09-09

### Corregido
- Favicon real de AnimeOnlineNinja.
- Ad-block reforzado con lista completa (~98.700 dominios) y exclusión de CDNs.

## [1.3.0] — 2026-09-09

### Cambiado
- **Arquitectura nueva**: los sitios se cargan en un **WebView nativo top-level**
  (antes iframe), permitiendo el bloqueo real de anuncios.

### Corregido
- Falsos positivos por coincidencia de subcadenas en el bloqueo.
- Inyección CSS robusta (Base64).

## [1.2.x] — 2026-09-09

### Corregido
- Error crítico `ERR_CONNECTION_REFUSED` (`androidScheme: file`).
- Pantalla blanca (uso correcto de `BridgeWebViewClient`).

## [1.1.0] — 2026-09-09

### Añadido
- Nuevos sitios, favicons reales, sonido de bienvenida y categoría Películas.

## [1.0.0] — 2026-09-09

### Añadido
- Versión inicial: splash, menú, selector de sitios, WebView, sonidos y ad-block básico.
