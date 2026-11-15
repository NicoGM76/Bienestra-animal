# Bienestar Animal Zipaquirá — Versión 1.0 RC

Aplicación Android híbrida para la gestión y seguimiento de bienestar animal en Zipaquirá, con prioridad en zonas rurales y diversidad de especies.

## Estado

- Rama de cierre: `final/1.0`.
- Versión Android: `1.0.0-rc1`.
- APK de pre-sustentación: se conserva separado y no se modifica.
- La rama 1.0 se valida mediante GitHub Actions antes de integrarla a `main`.

## Arquitectura

- Android + Kotlin.
- Interfaz HTML/CSS/JavaScript dentro de WebView endurecida.
- Firebase Authentication.
- Cloud Firestore.
- Firebase Storage para evidencia fotográfica.
- Firebase Cloud Messaging para push.
- Cloud Functions para envío privilegiado de push y auditoría.
- Firestore persistent cache para operación offline y sincronización posterior.

## Roles

### Ciudadano
- Registro e inicio de sesión.
- Tablero segmentado con tarjetas.
- Registro y edición segura de animales.
- Diversidad de especies: perro, gato, conejo, ave, caballo, bovino, cerdo, cabra y otros.
- Reportes de pérdida, hallazgo y presunto maltrato.
- Ubicación manual urbana/rural.
- Evidencia fotográfica por reporte.
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
- Evidencias fotográficas del reporte.
- Actividad y auditoría reciente.

### Veterinario
- Casos asignados.
- Filtros y resumen de carga.
- Inicio y cierre controlado de atención.
- Evidencias fotográficas del reporte.
- Ficha clínica.
- Historial clínico longitudinal por caso.
- Notificaciones internas y push.

## Offline

Firestore se inicializa con persistencia local y caché sin límite explícito. La app detecta el estado de conectividad desde Android y muestra modo online/offline. Las escrituras compatibles con Firestore pueden quedar pendientes y sincronizarse al recuperar Internet.

Consideración: el inicio de sesión inicial y la carga de contenido nunca consultado requieren conexión. El modo offline está pensado para una cuenta con sesión previa y datos ya disponibles en caché.

## Evidencias

El ciudadano puede seleccionar una fotografía desde Android. La imagen se sube a:

`caseEvidence/{caseId}/{evidenceId}`

El caso guarda `evidenceUrls`, `evidenceStoragePaths` y marcas de actualización. Admin y Veterinario pueden visualizar las imágenes desde el expediente.

## Notificaciones

La bandeja interna sigue siendo la fuente funcional principal. Cuando se despliegan las Cloud Functions, cada documento nuevo en `notifications` también dispara un push FCM hacia los dispositivos registrados del destinatario.

Los tokens se almacenan en:

`users/{uid}/devices/{deviceId}`

## Auditoría

La Cloud Function `auditCaseWrite` genera documentos en `auditLogs` ante cambios relevantes del caso. El administrador puede consultar las acciones recientes desde su tablero.

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
2. Publicar `storage.rules`.
3. Verificar Authentication Email/Password.
4. Desplegar Functions si se quiere demostrar push y auditoría backend:
   `firebase deploy --only functions`
5. Generar `demoDebug` o el build final seleccionado.
6. Ejecutar el flujo completo de los tres roles.

## Flujo final recomendado

Ciudadano reporta y adjunta foto → Admin revisa IA/prioridad y asigna → Veterinario consulta evidencia, atiende y registra ficha → ciudadano recibe actualización → auditoría conserva el cambio.

Equipo: Jorge Luis Osorio Silva y Nicolás García Mejía.
