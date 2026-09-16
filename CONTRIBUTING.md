# Contribuir a Danganime

¡Gracias por tu interés en contribuir a Danganime! Este documento explica cómo participar.

## Tipos de contribuciones

- **Reportes de bugs:** abre un issue con detalles (dispositivo, versión de Android, pasos para reproducir).
- **Sugerencias de sitios:** abre un issue indicando la categoría y la URL del sitio.
- **Pull requests:** envía los cambios directamente.

## Cómo contribuir

### 1. Haz un fork del repositorio

```bash
git clone https://github.com/JonathanDevHurtado/Danganime.git
cd Danganime
```

### 2. Crea una rama para tus cambios

```bash
git checkout -b feature/nueva-funcionalidad
```

### 3. Haz tus cambios

- Sigue el estilo de código existente.
- Actualiza la documentación si es necesario.
- No agregues comentarios innecesarios al código.

### 4. Compila y verifica

```bash
npm install
npx cap sync android
cd android && ./gradlew assembleDebug
```

### 5. Haz commit

```bash
git add .
git commit -m "Agregar nueva funcionalidad: descripción corta"
```

### 6. Push a tu rama

```bash
git push origin feature/nueva-funcionalidad
```

### 7. Abre un Pull Request

- Describe tus cambios.
- Menciona si resuelve algún issue.
- Incluye capturas si es relevante.

## Reglas de código

- **Frontend (`www/`):** HTML/CSS/JS vanilla, sin dependencias externas innecesarias.
- **Nativo (`MainActivity.java`):** Java, siguiendo el estilo existente.
- **Filtros:** documenta cualquier dominio/selector nuevo y por qué.
- **No comentarios** salvo que sean necesarios.

## Estructura del proyecto

```
Danganime/
├── www/                       # Frontend
│   ├── index.html
│   ├── css/style.css
│   └── js/app.js
├── android/app/src/main/
│   ├── java/com/danganime/app/MainActivity.java
│   └── assets/filters/
├── docs/                      # Documentación
├── logs/                      # Changelog por versión
└── tools/update_filters.py    # Regenerar listas
```

## Añadir un sitio nuevo

1. Edita `www/js/app.js` y agrega el sitio en la categoría correspondiente.
2. Coloca su favicon (64×64 PNG) en `www/favicons/`.
3. Verifica que sus recursos no estén bloqueados (añade el host a `SAFE_BASES` si es
   un reproductor legítimo).

## ¿Preguntas?

Si tienes dudas, abre un issue con la etiqueta `question`.

---

¡Gracias por contribuir! Tu ayuda hace que Danganime sea mejor para todos.
