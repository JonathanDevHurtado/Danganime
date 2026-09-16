# Guía de Construcción (Build)

## Requisitos Previos

### Software Necesario

| Software | Versión | Propósito |
|----------|---------|-----------|
| Node.js | 18+ | Ejecutar npm y Capacitor |
| npm | 9+ | Gestión de paquetes |
| Java JDK | 17 | Compilar código Android |
| Android SDK | API 34-35 | Compilar APK |
| Gradle | 8.2+ | Sistema de build |

### Variables de Entorno

```bash
export ANDROID_HOME=/home/usuario/android-sdk
export ANDROID_SDK_ROOT=/home/usuario/android-sdk
export JAVA_HOME=/ruta/a/jdk-17
```

---

## Estructura del Build

```
┌─────────────────┐
│  www/           │
│  (Frontend)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  capacitor      │
│  sync android   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  android/       │
│  (Capacitor     │
│   + Native)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  gradlew        │
│  assembleDebug  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Danganime.apk  │
└─────────────────┘
```

---

## Comandos de Build

### 1. Instalar Dependencias

```bash
cd /home/linius/Descargas/Danganime
npm install
```

### 2. Sincronizar con Capacitor

```bash
npx cap sync android
```

**Qué hace**:
- Copia archivos de `www/` a `android/app/src/main/assets/public/`
- Actualiza plugins de Capacitor
- Sincroniza configuración

### 3. Compilar APK

```bash
cd android
./gradlew assembleDebug
```

**Ubicación del APK**:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

### 4. Copiar APK a Directorio de Trabajo

```bash
cp android/app/build/outputs/apk/debug/app-debug.apk ./Danganime.apk
```

---

## Build Automatizado

### Script de Build Completo

```bash
#!/bin/bash
cd /home/linius/Descargas/Danganime

# Variables
export ANDROID_HOME=/home/linius/android-sdk
export ANDROID_SDK_ROOT=/home/linius/android-sdk
export JAVA_HOME=/home/linius/.sdkman/candidates/java/current

# 1. Sincronizar
echo "Sincronizando con Capacitor..."
npx cap sync android

# 2. Compilar
echo "Compilando APK..."
cd android
./gradlew assembleDebug

# 3. Copiar resultado
echo "Copiando APK..."
cp app/build/outputs/apk/debug/app-debug.apk ../Danganime.apk

echo "Build completado: Danganime.apk"
```

---

## Generate Icons

Para regenerar iconos desde `logo.png`:

```bash
# Tamaños requeridos
convert logo.png -resize 48x48 android/app/src/main/res/mipmap-mdpi/ic_launcher.png
convert logo.png -resize 72x72 android/app/src/main/res/mipmap-hdpi/ic_launcher.png
convert logo.png -resize 96x96 android/app/src/main/res/mipmap-xhdpi/ic_launcher.png
convert logo.png -resize 144x144 android/app/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert logo.png -resize 192x192 android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
```

---

## Troubleshooting de Build

### Error: "Could not find installation of TypeScript"

```bash
npm install -D typescript
```

### Error: "SDK processing version mismatch"

Actualizar Android SDK y cmdline-tools.

### Error: "Resource name character invalid"

Nombres de archivo deben ser minúsculas, números y guiones bajos.

### Error: "Deprecated API warning"

Es solo una advertencia, no afecta la compilación.

---

## Tamaño del APK

| Componente | Tamaño |
|------------|--------|
| Frontend (HTML/CSS/JS) | ~50KB |
| Logo | ~541KB |
| Audio welcome | ~169KB |
| Capacitor Runtime | ~2MB |
| Android Resources | ~1MB |
| **Total** | **~4.9MB** |

---

## Build Release (Firmado)

Para generar un APK firmado para producción:

```bash
# Generar keystore (una vez)
keytool -genkey -v -keystore danganime.keystore \
    -alias danganime -keyalg RSA -keysize 2048 -validity 10000

# Compilar release
./gradlew assembleRelease

# Firmar con apksigner
apksigner sign --ks danganime.keystore \
    app/build/outputs/apk/release/app-release-unsigned.apk
```
