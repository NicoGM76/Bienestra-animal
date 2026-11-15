# Changelog

Todos los cambios relevantes del proyecto Bienestar Animal Zipaquirá se registran en este archivo.

## [0.5.0-beta] - Gestión avanzada y continuidad clínica

### Añadido
- Compatibilidad de lectura para casos heredados con estados, prioridades y campos antiguos.
- `schemaVersion = 2` para nuevos casos y normalización progresiva al operar casos legados.
- Gestión administrativa con expediente detallado, búsqueda y filtros.
- Actualización acotada de prioridad institucional.
- Reasignación de casos mientras permanezcan en estado `assigned`.
- Historial clínico longitudinal en `cases/{caseId}/attentions/{attentionId}`.
- Recuperación de la ficha clínica anterior como registro legado cuando no existe historial longitudinal.
- Centro de notificaciones Firestore para Ciudadano y Veterinario.
- Avisos por asignación, reasignación, inicio de atención y finalización.
- Asistente de prioridad IA Beta basado en reglas explicables.
- Razón, puntaje y versión del asistente almacenados con el caso.
- Filtros de Veterinario por texto y estado.
- Filtros de Administrador por texto, estado, prioridad y tipo.
- Contratos automáticos para compatibilidad, gestión Admin, historial, notificaciones e IA Beta.

### Seguridad
- Admin solo puede modificar campos autorizados de asignación, reasignación o prioridad.
- Veterinario conserva escritura limitada a sus casos asignados y atención en curso.
- Las entradas del historial clínico son append-only desde el cliente.
- Las notificaciones solo pueden leerse por su destinatario y marcarse como leídas.
- La IA Beta no modifica automáticamente la prioridad final.

### Cambiado
- `versionCode` pasa de 6 a 7.
- `versionName` pasa de `0.4.2-beta` a `0.5.0-beta`.

### Pendiente
- Push del sistema operativo con FCM/Cloud Functions.
- Evidencias multimedia con Firebase Storage.
- Backend real de adopciones.
- Gestión de cuentas institucionales desde backend privilegiado.

---

## [0.4.2-beta] - Asignación operativa desde Administrador

### Añadido
- Asignación de casos recibidos desde el portal Administrador.
- Selección de veterinarios institucionales activos.
- Auditoría de asignación mediante `assignedBy`, `assignedAt`, `assignedTo` y `assignedRole`.
- Transición administrativa controlada `received -> assigned`.
- Regla Firestore específica que limita al administrador exclusivamente a los campos de asignación autorizados.
- Prueba de contrato para el flujo Admin -> Veterinario.

### Cambiado
- `versionCode` pasa de 5 a 6.
- `versionName` pasa de `0.4.1-beta` a `0.4.2-beta`.
- El portal Administrador deja de ser estrictamente de solo lectura y obtiene una única operación de escritura acotada: asignar casos recibidos.

### Se mantiene fuera de alcance
- Reasignación de casos ya iniciados.
- Edición general de casos por Administrador.
- Creación de cuentas institucionales desde la app.
- Historia clínica longitudinal independiente del caso.
- Adjuntos multimedia.

---

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
