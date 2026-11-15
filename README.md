# Bienestar Animal Zipaquirá — Versión 1.0 RC

Aplicación Android híbrida para la gestión y seguimiento de bienestar animal en Zipaquirá, con prioridad en zonas rurales y diversidad de especies.

## Estado

- Rama de cierre: `final/1.0`.
- Versión Android: `1.0.0-rc1`.
- APK de pre-sustentación: se conserva separado y no se modifica.
- La rama 1.0 se valida mediante GitHub Actions antes de integrarla a `main`.

## Arquitectura

- Android + Kotlin.
- El flavor activo se expone mediante `BuildConfig.ENVIRONMENT` para distinguir `demo` y `prod`.
- Interfaz HTML/CSS/JavaScript dentro de WebView endurecida.
- Firebase Authentication.
- Cloud Firestore.
- Firebase Authentication + Cloud Firestore como núcleo desplegado en plan Spark.
- Notificaciones internas en Firestore con proyección a notificación local Android.
- Firebase Storage, FCM remoto y Cloud Functions quedan como ampliaciones preparadas, no activas en la versión académica gratuita.
- Firestore persistent cache para operación offline y sincronización posterior.

## Roles

### Ciudadano
- Registro e inicio de sesión.
- Tablero segmentado con tarjetas.
- Registro y edición segura de animales.
- Diversidad de especies: perro, gato, conejo, ave, caballo, bovino, cerdo, cabra y otros.
- Reportes de pérdida, hallazgo y presunto maltrato.
- Ubicación manual urbana/rural.
- Evidencia fotográfica: interfaz preparada, no activa en el build académico gratuito.
- Seguimiento de casos.
- Notificaciones internas.
- Adopciones simuladas con solicitud local para demostración.
- Operación con caché offline después de una sesión válida.

### Administrador
- Tablero institucional.
- Métricas de usuarios, animales y casos.
- Búsqueda y filtros.
- Expediente por caso.
- Prioridad institucional.
- IA Beta explicable y no vinculante.
- Asignación y reasignación controlada.
- Evidencia multimedia reservada para infraestructura ampliada.
- Actividad reciente disponible cuando exista fuente de auditoría desplegada.

### Veterinario
- Casos asignados.
- Filtros y resumen de carga.
- Inicio y cierre controlado de atención.
- Evidencia multimedia reservada para infraestructura ampliada.
- Ficha clínica.
- Historial clínico longitudinal por caso.
- Notificaciones internas y notificaciones locales Android al consultar avisos nuevos.

## Offline

Firestore se inicializa con persistencia local y caché sin límite explícito. La app detecta el estado de conectividad desde Android y muestra modo online/offline. Las escrituras compatibles con Firestore pueden quedar pendientes y sincronizarse al recuperar Internet.

Consideración: el inicio de sesión inicial y la carga de contenido nunca consultado requieren conexión. El modo offline está pensado para una cuenta con sesión previa y datos ya disponibles en caché.

## Evidencias

La versión académica gratuita no activa Firebase Storage porque el proyecto se mantiene en Spark y no vincula facturación. El flujo principal de reportes funciona sin archivos adjuntos. La arquitectura de evidencia multimedia queda preparada para una futura infraestructura ampliada.

## Notificaciones


La bandeja interna en Firestore es la fuente funcional principal. Cuando la app consulta avisos nuevos no leídos, Android los refleja mediante una notificación local en el canal `case_updates`. Esto permite demostrar avisos del sistema sin Cloud Functions ni Blaze.

El código de FCM y registro de dispositivos se conserva como ampliación futura, pero no se presenta como push remoto activo en la versión académica gratuita.

## Auditoría

La auditoría automática basada en Cloud Functions queda preparada pero no desplegada en Spark. No se presenta como funcionalidad backend activa en la versión académica gratuita.

## Adopciones


El módulo de adopciones se mantiene deliberadamente como simulación funcional para la presentación 1.0. Las solicitudes se guardan localmente en el dispositivo y se identifican claramente como simuladas. No se presenta como backend productivo.

## Compatibilidad

Configuración Android:

- `minSdk = 23`
- `compileSdk = 35`
- `targetSdk = 35`

El repositorio incluye una matriz de smoke tests para API 26, 33 y 35. La entrega solo debe afirmar compatibilidad en las APIs que terminen con prueba exitosa.

## Despliegue Firebase antes del APK final

1. Publicar `firestore.rules`.
2. Verificar Authentication Email/Password.
3. Mantener el proyecto en plan Spark sin facturación.
4. No desplegar Storage ni Functions en la versión académica gratuita.
5. Generar `demoDebug` o el build final seleccionado.
6. Ejecutar el flujo completo de los tres roles y la prueba offline.

## Flujo final recomendado


Ciudadano reporta y adjunta foto → Admin revisa IA/prioridad y asigna → Veterinario consulta evidencia, atiende y registra ficha → ciudadano recibe actualización → auditoría conserva el cambio.

Equipo: Jorge Luis Osorio Silva y Nicolás García Mejía.
