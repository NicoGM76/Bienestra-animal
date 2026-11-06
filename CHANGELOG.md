# Changelog

Todos los cambios relevantes del proyecto Bienestar Animal Zipaquirá se registran en este archivo.

## [0.4.1-beta] - Expansión institucional de pre-sustentación

### Añadido
- Resumen de carga veterinaria con casos asignados, en atención y finalizados.
- Vista de ficha clínica por caso asignado.
- Registro veterinario de diagnóstico, observaciones, tratamiento y recomendaciones.
- Persistencia de la atención clínica dentro del caso en Firestore.
- Restricción de escritura clínica al veterinario asignado y solo durante `in_progress`.
- Panel administrativo con distribución de cuentas por rol.
- Panel administrativo con distribución de casos por estado.
- Actividad institucional ampliada y profesional asignado visible en casos recientes.
- Contratos automáticos para la expansión Veterinario/Admin.

### Cambiado
- `versionCode` pasa de 4 a 5.
- `versionName` pasa de `0.4.0-beta` a `0.4.1-beta`.
- Los portales institucionales dejan de ser vistas mínimas y pasan a soportar un flujo operativo demostrable.

### Se mantiene fuera de alcance
- Asignación de casos desde el portal administrador.
- Historia clínica longitudinal independiente del caso.
- Prescripción farmacológica estructurada.
- Adjuntos multimedia.
- Coordinador y funcionario de campo.

---

## [0.4.0-beta] - Pre-sustentación

### Añadido
- Inicio de sesión con lectura real de `users/{uid}`.
- Validación de `status`, `roles` y `activeRole`.
- Enrutamiento por rol para:
  - `citizen`
  - `veterinarian`
  - `administrator`
- Portal Veterinario.
- Consulta de casos asignados mediante `assignedTo`.
- Flujo veterinario:
  - `assigned -> in_progress`
  - `in_progress -> completed`
- Portal Administrador de solo lectura.
- Métricas reales de usuarios, animales y casos.
- Listados institucionales acotados desde Firestore.
- Firestore Rules por rol con denegación por defecto.
- Limpieza de datos visibles y bloqueo de pantallas protegidas al cerrar sesión.
- Pruebas de contrato para autenticación, roles, Firestore, Veterinario y Administrador.
- Workflow de GitHub Actions para ejecutar la suite de pruebas de contrato en la rama Beta.

### Cambiado
- `versionCode` pasa de 3 a 4.
- `versionName` pasa de `0.3.0` a `0.4.0-beta`.
- El login deja de asumir que toda cuenta es ciudadana.
- La documentación de Firebase incluye cuentas institucionales de prueba.

### Eliminado
- Botón y texto de “Confirmar en mapa”.
- Promesa de integración de mapa dentro del flujo actual.

### Se mantiene pendiente
- Compatibilidad completa en Android antiguo.
- Offline con Room/WorkManager.
- Evidencias multimedia con Firebase Storage.
- IA de priorización.
- Notificaciones FCM.
- Backend real de adopciones.
- Roles Coordinador y Funcionario de campo.
- Creación/gestión de cuentas institucionales desde la app.

---

## [0.3.0] - Alpha estable

### Añadido
- Registro ciudadano real con Firebase Authentication.
- Perfil ciudadano en Firestore.
- Inicio y cierre de sesión.
- Registro y consulta de animales.
- Ubicación rural manual.
- Reportes de pérdida, hallazgo y presunto maltrato.
- Consulta de reportes.
- Identidad protegida en reportes de presunto maltrato.
- WebView endurecida.
- Firestore Rules iniciales para datos del ciudadano.
- Catálogo de adopciones identificado explícitamente como Demo.

### Estado
Esta versión quedó preservada en:
- rama `main`;
- tag `alpha-v0.3.0`.
