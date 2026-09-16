# Solución de Problemas

## Errores Comunes

### 1. ERR_CONNECTION_REFUSED

**Mensaje**:
```
La pagina web de https://localhost/ no se pudo cargar porque: 
net:ERR_CONNECTION_REFUSED
```

**Causa**: Capacitor configurado con `androidScheme: "https"`

**Solución**:
```json
// capacitor.config.json
{
  "server": {
    "androidScheme": "file"
  }
}
```

---

### 2. Logo no aparece en Splash

**Problema**: El logo se ve bugeado o no aparece.

**Solución**: Verificar que:
1. `logo.png` existe en `www/`
2. El CSS tiene las clases correctas:
   - `.splash-logo-container` (contenedor)
   - `.splash-logo-img` (imagen)

---

### 3. Build falla con "TypeScript not found"

**Solución**:
```bash
npm install -D typescript
```

---

### 4. Build falla con resource name invalid

**Mensaje**: `'-' is not a valid file-based resource name character`

**Solución**: Renombrar archivos con guiones a guiones bajos:
```bash
# Mal
ic_launcher-playstore.png

# Bien
ic_launcher_playstore.png
```

---

### 5. Anuncios no se bloquean / páginas no cargan (iframes)

**Causa (histórica, solucionada en v1.3.0):**
1. Los sitios se cargaban en un `<iframe>` dentro de la app.
   Muchos sitios envían `X-Frame-Options` / CSP y lo bloquean (verificar con `curl -sI` el sitio).
2. En un iframe cross-origin **no se puede inyectar JS/CSS**, así que el ad-blocker no actuaba sobre la página.
3. `isAdUrl()` usaba `contains(dominio)`: dominios cortos bloqueaban sitios legítimos
   (`turn.com` → `return.com`), rompiendo la carga.
4. La inyección CSS en Java escapaba mal las comillas (solo la 1ª) → JS inválido y fallaba en silencio.

**Solución actual:**
- Los sitios se abren **top-level** en un WebView nativo (overlay), no en iframe.
- Bloqueo por **host exacto/sufijo**, sin substrings ni keywords.
- CSS inyectado **en Base64** (`atob`).
- El bloqueador vive en `MainActivity.java` (`AdBlockWebViewClient`) y las listas en `assets/filters/`.

**Verificar** (en dispositivo):
```bash
adb logcat -s Danganime
# Esperado al abrir: "Bloqueador listo: 177 dominios, CSS ... bytes"
```

---

### 5b. Un sitio concreto no abre en el navegador nativo

- Revisar si el sitio redirige a una app o a un dominio distinto.
- Probar el botón ↗ (abrir externo) para confirmar que el sitio funciona.
- El WebView usa el User-Agent de Android; algunos sitios sirven versiones distintas por UA.
- Revisar consola de Logcat por `onReceivedError` (no se interceptan errores hoy).

---

### 5c. "Danganime no responde" (ANR) o carga lenta al iniciar

**Causa**: la carga de la lista de dominios se hacía en el hilo principal con procesamiento pesado (regex por línea, lowercase, comprobaciones de bases) → ~7.9 s bloqueando la UI.

**Solución (v1.6.1)**:
- Lista pre-filtrada en build-time (`filters/blocked_domains_clean.txt`); el runtime solo agrega líneas.
- Carga en segundo plano (`new Thread(..., "filters-loader")`) con referencias volátiles.
- `noCompress 'txt'` en `build.gradle`.
- Verificación: `adb logcat -s Danganime` → `Bloqueador listo: 98714 dominios red ... (~1400 ms, hilo 'filters-loader')` y **0 ANR**.

---

### 6. App se ve negra al iniciar

**Solución**:
1. Verificar que `index.html` existe en `www/`
2. Ejecutar `npx cap sync android`
3. Recompilar el APK

---

### 7. Fullscreen no funciona

**Causa**: El sitio web no soporta fullscreen.

**Solución**: Verificar que el iframe tiene:
```javascript
iframe.setAttribute('allow', 'autoplay; fullscreen; picture-in-picture');
```

---

### 8. Sonido no se reproduce

**Causas posibles**:
1. El archivo `welcome.m4a` no existe
2. El navegador bloquea auto-play
3. El AudioContext está suspendido

**Solución**: El sonido se activa con la primera interacción del usuario.

---

### 9. Logo no se ve bien en todos los dispositivos

**Solución**: Regenerar iconos:
```bash
convert logo.png -resize 48x48 android/app/src/main/res/mipmap-mdpi/ic_launcher.png
convert logo.png -resize 72x72 android/app/src/main/res/mipmap-hdpi/ic_launcher.png
convert logo.png -resize 96x96 android/app/src/main/res/mipmap-xhdpi/ic_launcher.png
convert logo.png -resize 144x144 android/app/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert logo.png -resize 192x192 android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
```

---

### 10. App no carga después de actualizar

**Solución**:
```bash
# Limpiar build anterior
cd android
./gradlew clean

# Sincronizar de nuevo
cd ..
npx cap sync android

# Recompilar
cd android
./gradlew assembleDebug
```

---

## Comandos Útiles

### Verificar entorno
```bash
node --version        # Debe ser 18+
npm --version         # Debe ser 9+
java --version        # Debe ser 17+
echo $ANDROID_HOME    # Debe estar configurado
```

### Limpiar y reconstruir
```bash
cd android && ./gradlew clean && cd ..
npx cap sync android
cd android && ./gradlew assembleDebug
```

### Ver logs de Android
```bash
adb logcat | grep -i danganime
```

### Instalar APK directamente
```bash
adb install -r Danganime.apk
```

---

## Rendimiento en Gama Baja

La app está optimizada para dispositivos con:
- 2GB RAM mínimo
- Android 8.0+
- Procesador quad-core

**Optimizaciones**:
1. Sin librerías externas pesadas
2. CSS hardware-accelerated
3. JavaScript vanilla
4. APK mínimo (~5MB)
5. Sin animaciones complejas
