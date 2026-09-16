# Danganime - Documentación

## Índice General

### Documentación Principal

| # | Archivo | Contenido |
|---|---------|-----------|
| 1 | [Arquitectura](./01-arquitectura.md) | Estructura y diseño de la app |
| 2 | [Estructura de Archivos](./02-estructura-archivos.md) | Ubicación de cada archivo |
| 3 | [Funcionalidades](./03-funcionalidades.md) | Qué hace cada parte |
| 4 | [Bloqueo de Anuncios](./04-bloqueo-anuncios.md) | Cómo funciona el ad-block |
| 5 | [Fullscreen y Video](./05-fullscreen-video.md) | Modo inmersivo |
| 6 | [Guía de Build](./06-guia-build.md) | Cómo compilar el APK |
| 7 | [Solución de Problemas](./07-solucion-problemas.md) | Errores y soluciones |

### Log de Actualizaciones

| # | Versión | Fecha | Cambios |
|---|---------|-------|---------|
| 1 | [v1.0.0](../logs/v1.0.0.md) | 09/09/2026 | Versión inicial |
| 2 | [v1.1.0](../logs/v1.1.0.md) | 09/09/2026 | Nuevos sitios, favicon, fix logo |
| 3 | [v1.2.0](../logs/v1.2.0.md) | 09/09/2026 | Fix localhost, documentación |
| 4 | [v1.3.0](../logs/v1.3.0.md) | 09/09/2026 | Navegador nativo, fix páginas + ad-block |
| 5 | [v1.3.1](../logs/v1.3.1.md) | 09/09/2026 | Ad-block reforzado (probado), favicon AnimeOnlineNinja |
| 6 | [v1.4.0](../logs/v1.4.0.md) | 09/09/2026 | Audio bienvenida nuevo, Manga en-app, fixes ads, auditoría |
| 7 | [v1.5.0](../logs/v1.5.0.md) | 09/09/2026 | Botón Inicio 🏠, sin cuadrado flotante |
| 8 | [v1.5.1](../logs/v1.5.1.md) | 09/09/2026 | Fix JW Player (video), Inicio → menú app |
| 9 | [v1.6.1](../logs/v1.6.1.md) | 09/09/2026 | Fix ANR/lentitud, servidores de video, Ninjanime www1→ww3 |
| 10 | [v1.6.2](../logs/v1.6.2.md) | 09/09/2026 | Anti-popunder |
| 11 | [v1.6.4](../logs/v1.6.4.md) | 09/09/2026 | Servidores AnimeNinja + iconos |
| 12 | [v1.6.5](../logs/v1.6.5.md) | 09/09/2026 | Guard anti-popunder sin romper reproductores |
| 13 | [v1.7.2](../logs/v1.7.2.md) | 15/09/2026 | Nuevos sitios + Novelas Ligeras |
| 14 | [v1.7.3](../logs/v1.7.3.md) | 15/09/2026 | Verificación reproducción + guard |
| 15 | [v1.8.0](../logs/v1.8.0.md) | 16/09/2026 | Runtime volumen 100% y Pluto sin video inferior |
| 16 | [v1.8.1](../logs/v1.8.1.md) | 16/09/2026 | Restaurado video superior de Runtime |
| 17 | [v1.8.3](../logs/v1.8.3.md) | 16/09/2026 | Pluto: pausar video persistente |
| 18 | [v1.8.4](../logs/v1.8.4.md) | 16/09/2026 | Runtime: quitar aviso Google Play |
| 19 | [v1.8.6](../logs/v1.8.6.md) | 16/09/2026 | Pluto: pausa persistente sin romper reproducción |
| 20 | [v1.8.8](../logs/v1.8.8.md) | 16/09/2026 | Pluto: fix reproducción en horizontal |
| 21 | [v1.8.9](../logs/v1.8.9.md) | 16/09/2026 | Pluto: scroll en el inicio (horizontal) |
| 22 | [v1.9.0](../logs/v1.9.0.md) | 16/09/2026 | Pluto: barra de reproducción en pantalla completa |
| 23 | [v1.9.1](../logs/v1.9.1.md) | 16/09/2026 | Fix layout barra de Pluto (vertical/horizontal) |
| 24 | [v1.9.2](../logs/v1.9.2.md) | 16/09/2026 | Pluto: ocultar marcas de anuncios + auto-skip |
| 25 | [v1.9.3](../logs/v1.9.3.md) | 16/09/2026 | Pluto: marcas visibles + barra auto-ocultable |

---

## Resumen del Proyecto

**Danganime** es una aplicación Android para ver anime, leer manhwas/novelas y manga, así como ver películas.

### Características Clave

- Sitios abiertos en un **WebView nativo** (top-level) dentro de la app
- Bloqueo de anuncios por **host-matching** (~98.7k dominios) + CSS cosmético + JS
- Modo fullscreen inmersivo
- Animaciones de rebote
- Efectos de sonido
- Diseño optimizado para gama baja
- APK ligero (~5MB)

### Tecnologías

- HTML5 / CSS3 / JavaScript vanilla
- Capacitor 6
- Android SDK (Java)
- Gradle 8.2

### Estructura Rápida

```
Danganime/
├── www/           ← Frontend
├── android/       ← Código nativo Android
├── docs/          ← Esta documentación
├── log actualizacion/  ← Historial de versiones
├── Danganime.apk  ← APK compilado
└── logo.png       ← Logo de la app
```
