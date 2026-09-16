# Política de Seguridad

## Reportar vulnerabilidades

Si descubres una vulnerabilidad de seguridad en Danganime, por favor **no** la
publiques en un issue público. Contacta al mantenedor directamente por email.

### Qué incluir en el reporte

- Descripción de la vulnerabilidad.
- Pasos para reproducirla.
- Versión afectada.
- Posible impacto.
- Sugerencia de solución (si la tienes).

### Qué esperar

- Confirmación de recibido dentro de 48 horas.
- Evaluación de la gravedad dentro de 1 semana.
- Fix o mitigación según la gravedad.
- Créditos en el changelog (si lo deseas).

## Política de versiones

- Las vulnerabilidades se corrigen en la versión más reciente.
- No se proporcionan parches para versiones anteriores.
- Se recomienda usar siempre la última versión.

## Medidas de seguridad de la app

- `android:allowBackup="false"` (no se respaldan cookies/historial del WebView).
- Sin `addJavascriptInterface` expuesto.
- Acceso a archivos locales restringido (`allowFileAccessFromFileURLs=false`,
  `allowUniversalAccessFromFileURLs=false`).
- Comunicación app ↔ nativo mediante un esquema propio (`danganime://`), sin reemplazar
  el `WebViewClient` de Capacitor.

## Alcance

Esta política aplica solo al código fuente de Danganime. No aplica a:
- Los sitios de terceros que la app muestra.
- El WebView del sistema.
- Otras dependencias de terceros.

## Contacto

- **Email:** JonathanHurtadoDev@proton.me
- **GitHub:** [@JonathanDevHurtado](https://github.com/JonathanDevHurtado)
