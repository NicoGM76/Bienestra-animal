# Compatibilidad Android — Bienestar Animal Zipaquirá

## Política de soporte

La aplicación se diseña para equipos modestos y conectividad limitada. La versión de desarrollo posterior al Avance 1 usa `minSdk = 23` (Android 6.0) y mantiene `targetSdk = 35` / `compileSdk = 35` mientras se valida la migración de dependencias.

El taller de compatibilidad parte del estado auditado anterior (`minSdk = 24`) y usa Android 7 / API 24 como dispositivo antiguo de referencia. Bajar el mínimo a API 23 es una adaptación posterior del proyecto, no una alteración retroactiva de la evidencia de auditoría.

## Matriz mínima de pruebas

| Entorno | Uso | Estado esperado |
|---|---|---|
| Android 6 / API 23 | Mínimo soportado del producto | Instalación, arranque y navegación básica |
| Android 7 / API 24 | Referencia de dispositivo rural antiguo | Instalación, arranque, login simulado, registro de animal y ubicación rural |
| Android 15 / API 35 | Entorno moderno del taller | Mismas funciones del APK de prueba |
| Android 16 / API 36 | Compatibilidad moderna adicional | Ejecutar la misma versión cuando exista emulador disponible |

## Escenarios de recursos limitados

Además de la versión del sistema se deben observar:

- **red lenta o intermitente:** la interfaz local debe arrancar sin depender de Internet;
- **almacenamiento limitado:** evitar archivos multimedia innecesarios y limpiar temporales tras sincronización futura;
- **RAM limitada:** no cargar colecciones completas ni múltiples imágenes de alta resolución;
- **WebView antiguo:** evitar sintaxis JavaScript innecesariamente moderna y mantener pruebas de compatibilidad;
- **sin conexión:** en etapas posteriores Room + WorkManager mantendrán una cola local de sincronización.

## Seguridad WebView

Los archivos HTML/CSS/JS se sirven mediante `WebViewAssetLoader` sobre `https://appassets.androidplatform.net/assets/`. Se deshabilitan `file://`, acceso a contenido local y tráfico HTTP en claro. El puente JavaScript/Kotlin expone únicamente funciones pequeñas y valida mensajes antes de mostrarlos.

## Prueba del taller

Para demostrar compatibilidad, se debe instalar **el mismo APK** en el emulador moderno y en Android 7/API 24, y registrar evidencia de:

1. instalación;
2. inicio;
3. login simulado;
4. registro básico de animal;
5. ubicación rural y guardado simulado.

El hash SHA-256 del APK puede usarse como evidencia complementaria de que el archivo probado es idéntico en ambos equipos.
