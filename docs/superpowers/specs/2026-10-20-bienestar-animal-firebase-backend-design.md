# Bienestar Animal Zipaquirá — Diseño de arquitectura Firebase y backend

Fecha: 2026-10-20

## 1. Propósito

Diseñar la arquitectura técnica de una plataforma de gestión y bienestar animal para el municipio de Zipaquirá, Cundinamarca, con prioridad en zonas rurales y posibilidad de convertirse en una aplicación real para la Alcaldía.

El sistema debe ser usable en teléfonos modestos, tolerar conectividad intermitente, proteger información sensible, mantener trazabilidad institucional y permitir que ciudadanos, funcionarios de campo, veterinarios, coordinadores y administradores trabajen con permisos distintos.

La primera entrega formal para la Alcaldía es un APK, pero la arquitectura debe quedar preparada para transferencia institucional, operación con datos reales y evolución posterior.

## 2. Estado actual del proyecto

El Avance 1 ya existe como proyecto Android Studio híbrido:

- Kotlin como aplicación Android.
- HTML, CSS y JavaScript dentro de `app/src/main/assets/`.
- `WebView` como contenedor de la interfaz.
- `JavascriptInterface` como primer puente JavaScript ↔ Kotlin.
- Sin autenticación real, base de datos, persistencia, GPS, cámara, sincronización offline ni módulos funcionales de backend.
- Estado del Avance 1: `minSdk = 24`, `targetSdk = 35`, JVM 17.
- Objetivo de compatibilidad aprobado: bajar a `minSdk = 23` (Android 6.0), porque Firebase actual requiere Android 6.0/API 23 o superior.
- Matriz mínima de pruebas: Android 6/API 23, Android 7/API 24 o 25 y Android 16/API 36. Android 17 se usa como prueba adicional mientras sea beta, no como requisito de producción.

La arquitectura propuesta extiende esa base en lugar de reemplazarla.

## 3. Principios de diseño

1. **Interfaz sencilla.** Pedir solo datos necesarios y mostrar campos condicionales cuando aporten valor.
2. **Offline real.** El sistema debe seguir siendo útil con mala conectividad, especialmente en trabajo de campo.
3. **Seguridad por defecto.** Un usuario autenticado no obtiene acceso general; cada lectura y escritura depende de rol, asignación y contexto.
4. **Trazabilidad.** Los reportes, cambios de estado, prioridades, responsables e historias clínicas no se sobrescriben silenciosamente.
5. **Automatización asistida.** Reglas, puntuaciones e IA pueden recomendar, pero no toman decisiones delicadas de manera autónoma.
6. **Transferibilidad.** Firebase, firma del APK, documentación y configuración deben poder pasar de una cuenta del equipo a una cuenta institucional.
7. **Rendimiento y costo controlados.** Evitar descargas masivas, multimedia sin compresión y consultas innecesarias.
8. **Accesibilidad.** Estados importantes se comunican con texto, iconos y forma, no solo color.


## 3.1 Compatibilidad con dispositivos rurales

La aplicación debe priorizar teléfonos modestos y conectividad limitada. La compatibilidad objetivo es:

- **Mínimo soportado:** Android 6.0 / API 23.
- **Referencia de dispositivo antiguo:** Android 7 / API 24 o 25.
- **Referencia moderna estable:** Android 16 / API 36.
- **Prueba adicional:** Android 17 beta cuando el entorno de pruebas lo permita.
- Android 5 / API 21-22 no se considera soportado porque la versión actual de Firebase para Android requiere API 23 o superior.
- `androidx.webkit` debe fijarse en una versión compatible con API 23; no debe actualizarse automáticamente a una versión cuyo `minSdk` sea 24 o superior sin revisar esta política.
- Las pruebas de compatibilidad deben cubrir arranque, navegación WebView, formularios, almacenamiento local, sincronización, cámara/archivos cuando se implementen, uso con red lenta/sin red y consumo razonable de almacenamiento.

## 4. Arquitectura elegida

Se adopta la arquitectura híbrida:

```text
HTML + CSS + JavaScript
        │
        │ JavascriptInterface
        ▼
      Kotlin
        │
        ├── Room / almacenamiento privado / WorkManager
        │
        └── Firebase SDK
                │
                ├── Authentication
                ├── Cloud Firestore
                ├── Storage
                ├── Cloud Messaging
                ├── App Check
                ├── Crashlytics
                └── Cloud Functions
                        │
                        └── servicios de IA cuando aplique
```

### 4.1 Responsabilidades

**HTML/CSS/JS**
- Interfaz, navegación, formularios y accesibilidad.
- Presentación de estados y datos.
- Nunca contiene credenciales administrativas ni claves privadas.

**Kotlin**
- Sesión y autenticación en el dispositivo.
- Puente seguro entre WebView y Android.
- Cámara, GPS, archivos y permisos.
- Persistencia local con Room.
- Cola de sincronización y WorkManager.
- Comunicación con Firebase.
- Validaciones de cliente y gestión de errores de dispositivo.

**Cloud Functions**
- Operaciones sensibles que no deben depender del APK.
- Creación y gestión de cuentas institucionales.
- Asignación de roles y Custom Claims.
- Priorización asistida, sugerencias de asignación y automatizaciones.
- Generación de exportaciones e informes cuando corresponda.
- Integración segura con servicios de IA.

## 5. Organización interna de Kotlin

La aplicación se separará por responsabilidad, evitando concentrar toda la lógica en `MainActivity.kt` o `WebAppInterface.kt`.

```text
app/
  webview/
  auth/
  cases/
  animals/
  clinical/
  offline/
  media/
  location/
  notifications/
  data/
    local/
    remote/
  sync/
```

Los nombres exactos de paquetes pueden ajustarse durante la implementación, pero las fronteras funcionales deben mantenerse.

## 6. Modelo de identidad y roles

### 6.1 Roles

- `citizen`
- `fieldOfficer`
- `veterinarian`
- `coordinator`
- `administrator`

Una cuenta puede tener varios roles institucionales. `activeRole` controla la experiencia visible, no la seguridad real.

### 6.2 Creación de cuentas

- Ciudadanos: autorregistro con correo y contraseña.
- Personal institucional: creado por personal autorizado.
- Administrador: puede crear cuentas institucionales y asignar roles.
- Coordinador: puede gestionar funcionarios y veterinarios de su área, pero no crear administradores ni alterar permisos globales.

### 6.3 MFA

- Ciudadanos: correo + contraseña.
- Cuentas institucionales: segundo factor obligatorio en producción mediante un método MFA soportado por Firebase Authentication en el entorno de despliegue, con preferencia por TOTP frente a SMS cuando esté disponible.

## 7. Autorización y seguridad

La WebView también forma parte de la superficie de seguridad. La versión objetivo carga únicamente contenido propio de la aplicación, restringe navegación externa, expone un puente JavaScript mínimo con métodos específicos y valida todos los argumentos recibidos. Durante la implementación se migrará desde acceso directo `file://` hacia un mecanismo seguro como `WebViewAssetLoader`, permitiendo desactivar accesos de archivo/contenido que no sean necesarios.

La seguridad se aplica por capas:

1. Firebase Authentication identifica al usuario.
2. Custom Claims representan permisos institucionales relevantes.
3. Firestore Security Rules controlan acceso a cada documento.
4. La asignación del caso limita qué funcionario o veterinario puede leer/modificar.
5. Cloud Functions ejecutan operaciones sensibles.
6. App Check ayuda a validar que las peticiones provienen de la app legítima.
7. Audit Logs registran acciones relevantes.

Un funcionario no puede consultar todos los casos por pertenecer al rol `fieldOfficer`; solo accede a los casos asignados.

## 8. Identidad protegida en reportes sensibles

Los reportes delicados, especialmente presunto maltrato, usan **identidad protegida**.

- La Alcaldía conserva la identidad para trazabilidad y seguimiento.
- El presunto responsable no accede a la identidad.
- El funcionario de campo o veterinario recibe únicamente la información necesaria para atender el caso.
- Coordinador y administrador pueden acceder a la identidad cuando exista necesidad funcional.

La información operativa del caso y la identidad del reportante deben almacenarse con controles de acceso separados.

## 9. Modelo de datos lógico

La vista SQL mental equivalente incluye:

- `USERS`
- `USER_ROLES`
- `ANIMALS`
- `ANIMAL_CARETAKERS`
- `ANIMAL_RESPONSIBLE_HISTORY`
- `CASES`
- `CASE_UPDATES`
- `CASE_EVIDENCE`
- `CASE_HISTORY`
- `CLINICAL_RECORDS`
- `LOST_FOUND_REPORTS`
- `LOST_FOUND_MATCHES`
- `ANIMAL_CLAIMS`
- `ADOPTIONS`
- `ADOPTION_REQUESTS`
- `NOTIFICATIONS`
- `AUDIT_LOGS`
- `SYSTEM_SETTINGS`

En Firestore se usarán colecciones principales para entidades que necesiten consultas globales y subcolecciones cuando la información pertenezca estrictamente a un documento padre.

## 10. Colecciones Firestore propuestas

```text
users/
animals/
cases/
clinicalRecords/
lostFoundReports/
lostFoundMatches/
animalClaims/
adoptions/
adoptionRequests/
notifications/
auditLogs/
statistics/
settings/
```

### 10.1 `users/{uid}`

Campos principales:
- nombre y datos de contacto.
- `roles[]`.
- `activeRole`.
- tipo de cuenta.
- estado.
- fechas de creación/actualización.

### 10.2 `animals/{animalId}`

Campos principales:
- nombre.
- especie.
- raza.
- sexo.
- fecha aproximada de nacimiento.
- descripción física.
- responsable principal.
- origen.
- estado.
- fechas de creación/actualización.

Catálogo inicial de especies: perro, gato, conejo, ave, caballo, bovino, cerdo, cabra y otro. El catálogo es ampliable desde configuración institucional.

Estados operacionales previstos:
- `active`
- `lost`
- `found`
- `in_adoption`
- `adopted`
- `deceased`
- `institutional`

Un animal encontrado puede existir sin responsable conocido y permanecer bajo perfil institucional.

### 10.3 `cases/{caseId}`

Campos funcionales principales:
- tipo.
- descripción original.
- animal relacionado cuando exista.
- creador.
- ubicación, punto de referencia y coordenadas.
- cantidad y estado de los animales afectados.
- `userPriority`.
- `aiSuggestedPriority`.
- `finalPriority`.
- `aiConfidence` cuando aplique.
- estado.
- responsable asignado y rol requerido.
- indicador de identidad protegida.
- fechas de creación, actualización y cierre.
- versión para control de concurrencia.

Subcolecciones recomendadas:
- `updates`
- `evidence`
- `history`
- información privada del reportante con reglas más restrictivas.

### 10.4 Evidencias

Firestore almacena metadatos; Firebase Storage almacena los archivos reales.

Tipos admitidos:
- foto.
- video.
- audio.
- documento.

Las evidencias solo son visibles para usuarios con permiso sobre el caso.

## 11. Reporte ciudadano

Los campos base del reporte deben ser breves y condicionales:

- tipo de reporte.
- descripción.
- prioridad elegida o solicitud de sugerencia.
- cantidad de animales.
- estado del animal.
- ubicación dentro de Zipaquirá, diferenciando barrio/vereda y, cuando aplique, finca/predio/sector.
- punto de referencia.
- selección de ubicación en mapa; si el GPS está disponible puede proponer el punto y, si no, el usuario puede ubicarlo manualmente.
- fecha del hecho, con valor actual por defecto y edición solo cuando el evento ocurrió antes.
- fotos, video o audio opcionales como evidencia.

En casos de presunto maltrato se incluye información sobre:
- propietario/cuidador conocido o animal de calle.
- posible responsable o persona señalada, únicamente si el ciudadano la conoce.

El sistema evita obligar al usuario a repetir nombre, correo u otros datos ya conocidos por su cuenta.

## 12. Ciclo de vida de casos

Estados principales:

```text
RECEIVED
UNDER_REVIEW
ASSIGNED
IN_PROGRESS
WAITING_FOR_CITIZEN
FOLLOW_UP_REQUIRED
COMPLETED
REOPENED
```

Estados especiales:
- `WITHDRAWAL_REQUESTED`
- `DUPLICATE`
- `CLOSED_INSUFFICIENT_INFORMATION`

### 12.1 Flujo

1. Ciudadano crea el reporte.
2. Se valida y guarda localmente o se envía a Firebase.
3. Se calcula una prioridad sugerida.
4. Coordinador revisa y confirma/cambia la prioridad final.
5. El sistema propone responsable según rol, zona y carga.
6. Coordinador confirma o modifica la asignación.
7. Funcionario/veterinario atiende el caso.
8. Puede solicitar información adicional al ciudadano.
9. El responsable asignado puede finalizar el caso.
10. El coordinador puede reabrirlo cuando corresponda.

El ciudadano ve únicamente el avance general, no notas internas ni identidad del personal asignado.

## 13. Solicitudes de información adicional

Coordinador, funcionario asignado o veterinario asignado pueden solicitar información adicional indicando:

- qué información hace falta.
- por qué se necesita.

Si el ciudadano no responde, el sistema alerta según prioridad:

- crítica: 24 h.
- alta: 48 h.
- media: 72 h.
- baja: 120 h.

El sistema nunca cierra automáticamente el caso por falta de respuesta; una persona decide cómo continuar.

## 14. Finalización de casos

Para evitar formularios extensos, el responsable selecciona un resultado:

- resuelto.
- atendido.
- requiere seguimiento.
- no se pudo realizar.
- otro.

Puede adjuntar foto/documento y/o una nota corta cuando aporten valor. Fecha, hora, responsable, identificador y estado se registran automáticamente.

## 15. Retiro de reportes

El ciudadano puede solicitar retirar un reporte, pero no eliminarlo de la historia institucional. Se conserva trazabilidad, evidencias e historial de acciones.

## 16. Historial y auditoría

Cada cambio importante conserva:

- usuario o sistema que realizó la acción.
- acción.
- valor anterior y nuevo cuando aplique.
- fecha y hora.

La auditoría no es editable desde clientes normales.

## 17. Gestión de animales

### 17.1 Responsable y cuidadores

- Un responsable principal.
- Cuidadores/autorizados adicionales.
- Historial de responsables anteriores con fechas.

### 17.2 Historia clínica

Cada atención es una entrada independiente. Tipos iniciales:
- consulta.
- vacunación.
- desparasitación.
- esterilización.
- procedimiento.
- control.
- diagnóstico.
- emergencia.
- otro.

Una corrección clínica genera una nueva entrada vinculada a la anterior, sin borrar el registro original.

## 18. Perdidos, encontrados y reclamaciones

Un animal registrado que se pierde conserva su `animalId`; se crea un reporte relacionado.

Un animal encontrado sin propietario puede recibir un perfil institucional temporal.

La aplicación puede sugerir coincidencias usando:
- especie.
- sexo.
- color y rasgos.
- fecha.
- ubicación/distancia.
- fotografías cuando existan.

La coincidencia nunca se confirma automáticamente.

Para reclamar un animal, las evidencias principales son:
- fotos anteriores.
- registro previo.
- carnet veterinario.
- ubicación donde se perdió.
- información adicional pertinente.

La Alcaldía revisa y aprueba o rechaza la reclamación.

## 19. Adopciones

Un ciudadano puede solicitar publicar un animal en adopción, pero la publicación requiere aprobación institucional.

La solicitud de adopción será corta:
- datos de contacto ya disponibles en la cuenta.
- lugar donde vivirá el animal.
- experiencia previa.
- razón breve para adoptar.

Si se aprueba una adopción:
- se actualiza el estado del animal.
- se registra el nuevo responsable principal.
- se conserva el historial anterior.
- se cierra la publicación.
- se cierran las demás solicitudes relacionadas.

## 20. Prioridad, automatización e IA

El sistema utiliza tres niveles de apoyo:

1. **Reglas determinísticas** para factores evidentes de gravedad.
2. **Puntuaciones** para prioridad, asignación y coincidencias.
3. **IA** para interpretar descripciones y evidencia cuando aporte valor.

La IA puede sugerir una prioridad y explicar factores detectados. No puede:
- borrar un reporte.
- cerrarlo automáticamente.
- declarar culpabilidad.
- sancionar a un ciudadano.

Si la IA no está disponible, el reporte sigue entrando y se usa el motor de reglas más revisión humana.

## 21. Asignación sugerida

La asignación es mixta:

- El sistema propone responsable según rol, zona, disponibilidad y carga activa.
- El coordinador confirma o cambia la propuesta.

La lógica de asignación debe ser determinística y explicable; no requiere IA generativa.

## 22. Notificaciones

Se utilizan dos mecanismos:

- notificación interna persistente en Firestore.
- push mediante Firebase Cloud Messaging.

Eventos principales:
- nuevo reporte para coordinador.
- caso alto/crítico destacado.
- caso asignado o reasignado.
- solicitud de información adicional.
- respuesta del ciudadano.
- seguimiento pendiente.
- caso reabierto.
- caso completado para ciudadano.
- conflicto de sincronización.
- actividad sospechosa.
- error técnico relevante para administrador.

## 23. Offline y almacenamiento local

### 23.1 Ciudadanos

Pueden preparar reportes sin conexión. Datos y archivos permanecen como `PENDING` hasta que la sincronización se confirma.

### 23.2 Funcionarios y veterinarios

Solo se almacenan offline:
- casos asignados.
- datos necesarios del animal y caso.
- acciones pendientes.
- evidencias creadas durante la visita.

No se descarga toda la base institucional.

### 23.3 Tecnologías

- Room para datos estructurados locales.
- almacenamiento privado de la app para multimedia.
- WorkManager para sincronización confiable.

Estados de sincronización:
- `PENDING`
- `UPLOADING`
- `SYNCED`
- `ERROR`
- `CONFLICT`

## 24. Uso de almacenamiento en teléfonos modestos

- Los datos de texto se guardan localmente porque su tamaño es reducido.
- Las fotos se comprimen y redimensionan antes de conservarse/subirse.
- Videos y audios son opcionales y limitados.
- Los archivos temporales sincronizados se eliminan del almacenamiento local cuando Firebase confirma la carga.
- Si existe poco espacio, se advierte sin bloquear la posibilidad de reportar sin multimedia.

## 25. Conflictos de sincronización

Se adopta mezcla automática cuando dos cambios afectan datos distintos.

Si dos usuarios modifican el mismo dato incompatible:
- no se sobrescribe silenciosamente.
- se marca conflicto.
- el coordinador decide la versión final cuando sea necesario.

Cada entidad sincronizable mantiene versión, `updatedAt` y `updatedBy`.

## 26. Panel administrativo y estadísticas

El dashboard debe priorizar información operativa:

- casos activos.
- críticos.
- pendientes de asignación.
- casos en seguimiento.
- animales registrados.
- perdidos/encontrados.
- animales en adopción.
- adopciones completadas.
- tiempos medios de atención/resolución.
- distribución por tipo, prioridad, estado y zona.

No se presenta un ranking punitivo de funcionarios; se muestran cargas y métricas para distribuir trabajo.

Para evitar lecturas masivas, Cloud Functions mantiene contadores y resúmenes estadísticos precalculados.

## 27. Importación y exportación

### Exportación
- Excel/CSV para listados y análisis.
- PDF para resúmenes formales, actas e informes.

El acceso depende del rol y del tipo de información.

### Importación
La importación desde Excel pasa por:
1. validación.
2. vista previa.
3. detección de errores/duplicados.
4. confirmación.
5. importación.

Las operaciones de importación/exportación quedan auditadas.

## 28. Backups y retención

- Producción debe tener backups periódicos de Firestore y una política de conservación para Storage.
- Los tiempos de retención son configurables según tipo de dato.
- Los archivos temporales offline no se rigen por la retención institucional; se limpian tras sincronización confirmada.

## 29. Catálogos y configuración

`settings` y catálogos permiten cambiar sin recompilar:
- tiempos de alertas.
- límites de archivo.
- políticas de retención.
- especies habilitadas.
- tipos de reporte configurables cuando sea seguro hacerlo.

Estados internos críticos del sistema permanecen controlados por código para evitar configuraciones inválidas.

## 30. Sisbén

La primera versión no depende de integración directa con Sisbén.

Se prepara el modelo con estados:
- pendiente.
- verificado.
- incompleto.
- no aplica.
- rechazado.

Una integración institucional futura puede añadirse sin bloquear la primera versión.

## 31. Prevención de spam y abuso

El sistema puede detectar:
- volumen anormal de reportes.
- contenido repetido.
- patrones sospechosos.

Puede aplicar límites temporales y generar alertas, pero la decisión de advertir, restringir o suspender una cuenta es humana.

## 32. Entornos y despliegue

Se utilizan dos proyectos Firebase:

- **demo/pruebas** con información ficticia.
- **producción** para datos reales.

Una sola base de código usa configuraciones distintas por build variant/build type.

La demo debe incluir al menos:
- un ciudadano.
- un funcionario.
- un veterinario.
- un coordinador.
- un administrador.
- animales y casos ficticios suficientes para demostrar flujos completos.

## 33. APK, firma y transferencia institucional

- La versión release se firma con una clave protegida.
- Keystore y contraseñas nunca se suben a Git.
- Si la Alcaldía adopta la aplicación, la firma de producción y la propiedad del proyecto Firebase deben quedar bajo control institucional.
- La transferencia debe documentar servicios activos, propietarios, backups, Cloud Functions, Storage, Firestore, Authentication, FCM y App Check.

## 34. Observabilidad

Se incorpora Firebase Crashlytics para fallos de aplicación.

Los logs técnicos evitan datos personales innecesarios y registran identificadores funcionales y códigos de error.

Ejemplo aceptable:

```text
CASE_UPLOAD_FAILED caseId=CA0231 networkTimeout=true
```

## 35. Rendimiento y costos

- paginar listados.
- consultar solo documentos necesarios.
- evitar descargar colecciones completas.
- comprimir multimedia.
- usar estadísticas precalculadas.
- limpiar temporales.
- limitar reintentos y operaciones redundantes.

Estas reglas protegen tanto dispositivos modestos como costos de Firebase.

## 36. Privacidad y protección de datos

La solución debe incluir en producción:
- aviso de privacidad.
- finalidad del tratamiento.
- consentimiento cuando corresponda.
- control de acceso.
- retención configurable.
- mecanismos de actualización/corrección de datos.

La validación jurídica final de textos y políticas corresponde a la Alcaldía antes de operar con datos reales.

## 37. Manejo de errores

Principios:

- Una falla de IA no impide registrar un reporte.
- Una falla de red deja la acción pendiente localmente.
- Una falla de Storage no elimina el formulario ni los datos estructurados.
- Reintentos automáticos deben tener límites y retroceso progresivo.
- El usuario recibe mensajes claros: pendiente, enviando, sincronizado, error o conflicto.
- Operaciones administrativas parciales deben ser atómicas o compensadas mediante Cloud Functions para evitar estados inconsistentes.

## 38. Pruebas requeridas

### 38.1 Unitarias
- cálculo de prioridad por reglas.
- lógica de asignación sugerida.
- resolución de estados y validaciones.
- serialización de cola offline.

### 38.2 Instrumentadas Android
- WebView ↔ Kotlin bridge.
- Room.
- WorkManager.
- permisos de cámara/GPS.
- almacenamiento privado.

### 38.3 Firebase Emulator Suite
- Security Rules.
- Authentication.
- Firestore.
- Storage.
- Cloud Functions.

### 38.4 Integración
- crear reporte online/offline.
- sincronizar multimedia.
- asignar caso.
- solicitar información.
- cerrar y reabrir.
- cambio de rol activo sin elevar permisos.
- identidad protegida.

### 38.5 Conflictos
- dos dispositivos modificando campos diferentes.
- modificación concurrente del mismo estado.
- recuperación tras fallo de red a mitad de una carga.

### 38.6 Accesibilidad y rendimiento
- contraste.
- información no dependiente solo de color.
- tamaños táctiles adecuados.
- funcionamiento con conexión lenta.
- pruebas en dispositivo/emulador con recursos limitados.

## 39. Alcance excluido de la primera implementación

No forma parte de la primera versión:

- integración automática real con Sisbén.
- publicación en Google Play.
- backend SQL propio adicional a Firebase.
- decisiones autónomas de IA que cierren, rechacen o sancionen casos.
- reconocimiento biométrico definitivo de animales.
- descarga offline de toda la base institucional.
- integraciones externas no requeridas para los flujos aprobados.

## 40. Estrategia de implementación por etapas

Aunque esta especificación define la arquitectura completa, la implementación se ejecutará en etapas para mantener el proyecto verificable y aprendible:

1. **Fundación Android y seguridad de WebView:** estructura Kotlin, bridge seguro, configuración demo/prod y pruebas base.
2. **Identidad y datos núcleo:** Firebase Authentication, roles, usuarios, animales y Security Rules.
3. **Casos y evidencias:** reportes, Storage, historial, prioridad y permisos por asignación.
4. **Offline:** Room, cola de sincronización, WorkManager y resolución de conflictos.
5. **Módulos animales:** clínica, perdidos/encontrados, reclamaciones y adopciones.
6. **Automatización:** Cloud Functions, FCM, sugerencias de prioridad/asignación y capa de IA opcional.
7. **Administración:** dashboard, estadísticas, importación/exportación, auditoría y configuración.
8. **Endurecimiento y entrega:** App Check, Crashlytics, backups, pruebas de seguridad, APK release y documentación de transferencia.

Cada etapa debe terminar con pruebas automatizadas y una versión ejecutable antes de avanzar a la siguiente.

## 41. Criterios de éxito

La arquitectura se considera correctamente implementada cuando:

1. Los cinco roles pueden autenticarse con permisos correctos.
2. Un ciudadano puede crear un reporte online y offline sin perder información.
3. Las evidencias se almacenan de forma privada y sincronizan correctamente.
4. Un coordinador puede priorizar y asignar casos.
5. Funcionarios y veterinarios solo acceden a casos autorizados.
6. Un caso conserva historial completo y puede cerrarse/reabrirse sin perder trazabilidad.
7. El ciudadano ve únicamente el estado y las solicitudes de información pertinentes.
8. Animales, clínica, perdidos/encontrados, reclamaciones y adopciones conservan relaciones e historial.
9. FCM entrega notificaciones relevantes sin exponer datos sensibles.
10. Demo y producción permanecen separados.
11. Security Rules y Cloud Functions impiden elevación de privilegios desde el cliente.
12. El sistema sigue funcionando de forma degradada cuando no hay Internet o el servicio de IA no está disponible.
13. La solución puede transferirse a una cuenta institucional sin reescribir la arquitectura.
