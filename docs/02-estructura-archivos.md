# Estructura de Archivos

## Árbol del Proyecto

```
Danganime/
├── 📄 capacitor.config.json      # Configuración de Capacitor
├── 📄 package.json                # Dependencias npm
├── 📄 paginasdanganime.txt        # Lista de sitios web
├── 📄 logo.png                    # Logo de la aplicación
├── 📄 Danganime.apk               # APK compilado
│
├── 📁 www/                        # Frontend (archivos web)
│   ├── 📄 index.html              # Página principal
│   ├── 📄 logo.png                # Logo para la UI
│   ├── 📁 css/
│   │   └── 📄 style.css           # Estilos y animaciones
│   ├── 📁 js/
│   │   ├── 📄 app.js              # Lógica principal
│   │   └── 📄 sounds.js           # Efectos de sonido
│   └── 📁 sounds/
│       └── 📄 welcome.m4a         # Sonido de bienvenida
│
├── 📁 android/                    # Proyecto Android nativo
│   ├── 📄 build.gradle            # Configuración de Gradle
│   ├── 📄 settings.gradle         # Configuración de settings
│   ├── 📄 variables.gradle        # Variables de versión
│   └── 📁 app/
│       ├── 📄 build.gradle        # Build del módulo app
│       └── 📁 src/main/
│           ├── 📄 AndroidManifest.xml
│           ├── 📁 java/com/danganime/app/
│           │   └── 📄 MainActivity.java
│           ├── 📁 res/
│           │   ├── 📁 mipmap-*/     # Iconos de la app
│           │   ├── 📁 values/       # Strings, colores, estilos
│           │   └── 📁 xml/          # Configuración de red
│           └── 📁 assets/public/    # Copia de www/
│
├── 📁 Audios-Assets/
│   └── 📁 Bienvenida/
│       └── 📄 *.mp3               # Audio de bienvenida
│
└── 📁 docs/                       # Documentación
    ├── 📄 00-README.md
    ├── 📄 01-arquitectura.md
    ├── 📄 02-estructura-archivos.md
    ├── 📄 03-funcionalidades.md
    ├── 📄 04-bloqueo-anuncios.md
    ├── 📄 05-fullscreen-video.md
    ├── 📄 06-guia-build.md
    └── 📄 07-solucion-problemas.md
```

## Descripción de Archivos Principales

### Frontend (www/)

| Archivo | Tamaño | Descripción |
|---------|--------|-------------|
| `index.html` | ~3KB | Estructura HTML de la app: splash, menú, selector de sitios, WebView |
| `css/style.css` | ~15KB | Todos los estilos, animaciones bounce, tema oscuro |
| `js/app.js` | ~8KB | Lógica de navegación, carga de sitios, inyección de ad-block |
| `js/sounds.js` | ~3KB | Efectos de sonido con Web Audio API |
| `logo.png` | ~541KB | Logo de la aplicación |
| `sounds/welcome.m4a` | ~57KB | Sonido de bienvenida |

### Android (android/)

| Archivo | Descripción |
|---------|-------------|
| `MainActivity.java` | Código nativo principal: ad-block, fullscreen, immersive mode |
| `AndroidManifest.xml` | Permisos y configuración de la app |
| `network_security_config.xml` | Permite tráfico HTTP/HTTPS |
| `build.gradle` | Configuración de compilación |
| `ic_launcher*.png` | Iconos generados desde logo.png |

### Configuración

| Archivo | Descripción |
|---------|-------------|
| `capacitor.config.json` | Configuración de Capacitor (app ID, web dir) |
| `package.json` | Dependencias npm y scripts |
| `paginasdanganime.txt` | Lista de sitios web organizados por categoría |

## Codificación de Colores

| Color | Hex | Uso |
|-------|-----|-----|
| Rosa | `#ff6b9d` | Categoría Anime, botones principales |
| Morado | `#c44dff` | Categoría Manhwa, acentos |
| Cian | `#00d4ff` | Categoría Manga, información |
| Verde | `#00ff88` | Categoría Películas, éxito |
| Fondo | `#0f0f23` | Tema oscuro principal |
| Tarjetas | `#252547` | Fondo de tarjetas |
