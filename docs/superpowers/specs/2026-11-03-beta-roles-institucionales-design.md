# Beta de roles institucionales — Diseño

## Intención

Convertir la Alpha funcional de Bienestar Animal Zipaquirá en una primera Beta con acceso real por rol, conservando todos los flujos ciudadanos ya verificados y sin reconstruir el proyecto.

## Estado de partida

La rama `main` contiene la Alpha estable, etiquetada como `alpha-v0.3.0`.
La rama de trabajo es `beta/roles-institucionales`.

La Alpha ya tiene:
- Firebase Authentication real.
- Perfiles en `users/{uid}`.
- Registro y consulta de animales.
- Reportes y consulta de reportes.
- Ubicación rural ingresada manualmente.
- WebView + Kotlin + HTML/CSS/JavaScript.
- Firestore Rules con denegación por defecto.

## Objetivos de esta Beta

1. Mantener intactos los flujos existentes del ciudadano.
2. Hacer que el inicio de sesión consulte `users/{uid}` y use `activeRole`.
3. Enrutar a tres portales: `citizen`, `veterinarian` y `administrator`.
4. Crear un portal veterinario mínimo pero funcional con casos asignados.
5. Crear un portal administrador mínimo y funcional de lectura.
6. Endurecer Firestore Rules para que cada rol solo acceda a lo que le corresponde.
7. Mantener la restricción del proyecto: no usar GPS ni permisos de ubicación.

## No objetivos

- No crear todavía cuentas institucionales desde la app.
- No implementar coordinador ni funcionario de campo en este bloque.
- No implementar cambio de rol desde la interfaz.
- No implementar GPS, ubicación automática ni permisos `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`.
- No implementar IA de prioridad.
- No implementar Firebase Storage, multimedia ni offline en este bloque.
- No permitir al administrador editar o borrar datos en esta primera Beta.

## Enfoques considerados

### A. Portales solo visuales
Rápido, pero no demuestra control de acceso ni comportamiento real. Descartado.

### B. Roles reales + portales mínimos funcionales
El login lee el perfil de Firestore, valida `status`, `roles` y `activeRole`, y enruta al portal correcto. Veterinario y administrador consumen datos reales con reglas específicas. **Seleccionado.**

### C. RBAC completo con todos los roles
Más completo, pero agrega coordinador, funcionario, gestión institucional y auditoría antes de estabilizar la base. Descartado para este bloque Beta.

## Autenticación y perfil

`AuthOutcome.Success` pasa a incluir:
- `uid`
- `email`
- `name`
- `activeRole`
- `roles`

Tras `signInWithEmailAndPassword`, Kotlin consulta `users/{uid}`.

El inicio se rechaza y se cierra sesión si:
- no existe el perfil;
- `status != "active"`;
- `activeRole` está vacío;
- `activeRole` no pertenece a `roles`;
- el rol activo no está soportado por esta Beta.

El registro ciudadano continúa creando únicamente:
- `roles = ["citizen"]`
- `activeRole = "citizen"`
- `accountType = "citizen"`

Las cuentas de veterinario y administrador se crean manualmente en Firebase Authentication y Firestore durante esta Beta.

## Enrutamiento

Después de `auth:success`:

- `citizen` → `home`
- `veterinarian` → `vet-home`
- `administrator` → `admin-home`

La interfaz nunca decide el rol por sí sola. Solo consume el rol validado por Kotlin a partir de Firestore.

## Portal ciudadano

No se eliminan ni se rediseñan los flujos que ya funcionan:
- registrar animal;
- consultar animales;
- reportar pérdida;
- reportar hallazgo;
- reportar presunto maltrato;
- consultar reportes;
- cerrar sesión.

Se elimina la promesa visual `Confirmar en mapa · próximo avance` porque el proyecto no utiliza GPS. La ubicación sigue siendo manual.

## Portal veterinario

Funcionalidad de esta Beta:
- mostrar nombre/rol de la sesión;
- listar únicamente casos donde `assignedTo == request.auth.uid`;
- mostrar tipo, prioridad final, estado y ubicación rural del caso;
- permitir transición controlada de estado:
  - `assigned -> in_progress`
  - `in_progress -> completed`
- cerrar sesión.

Para las pruebas iniciales, `assignedTo` se carga manualmente en Firestore porque el rol coordinador todavía no forma parte de esta Beta.

El veterinario no recibe en la interfaz la identidad del ciudadano denunciante.

## Portal administrador

Funcionalidad de esta Beta:
- mostrar nombre/rol de la sesión;
- métricas reales de cantidad de usuarios, animales y casos;
- consultar una lista acotada de casos recientes;
- consultar una lista acotada de animales;
- consultar una lista acotada de usuarios;
- cerrar sesión.

El administrador comienza en modo lectura. No crea, edita, elimina ni asigna casos desde la app en este bloque.

## Modelo de casos

Los casos existentes siguen siendo válidos.

Para casos asignados se agregan opcionalmente:
- `assignedTo: String`
- `assignedRole: "veterinarian"`

No se exige migrar casos antiguos que todavía no estén asignados.

## Seguridad Firestore

### Usuarios
- ciudadano: puede crear su propio perfil ciudadano y leer su propio perfil;
- veterinario: puede leer su propio perfil;
- administrador: puede leer perfiles;
- ningún cliente puede cambiar `roles`, `activeRole` o `accountType` en esta Beta.

### Animales
- ciudadano: conserva lectura y creación de sus animales;
- administrador: lectura;
- veterinario: no obtiene lectura global de animales en este bloque.

### Casos
- ciudadano: crea y lee sus propios casos;
- veterinario: lee únicamente casos cuyo `assignedTo` coincide con su UID;
- veterinario: solo puede modificar `status` y `updatedAt` de sus casos asignados, respetando las transiciones permitidas;
- administrador: lectura de casos;
- eliminación continúa bloqueada.

Las reglas siguen con denegación por defecto.

## Arquitectura

Se conserva la arquitectura actual:
- HTML/CSS/JavaScript en WebView.
- Kotlin como puente y capa de validación.
- Firebase Authentication.
- Cloud Firestore.
- Sin Jetpack Compose.
- Sin GPS.

Se agregan componentes enfocados para no inflar `WebAppInterface` y los gateways ciudadanos.

## Compatibilidad de datos

No se borran colecciones ni registros existentes.
Los nuevos campos de asignación son opcionales.
Los usuarios ciudadanos actuales continúan siendo válidos.
La Beta debe poder leer los animales y casos creados durante la Alpha.

## Pruebas obligatorias

Antes de considerar este bloque terminado:
1. Todas las pruebas existentes deben seguir pasando.
2. Ciudadano existente inicia sesión y conserva animales/reportes.
3. Cuenta veterinaria entra a `vet-home`, no a `home`.
4. Veterinario solo ve casos asignados a su UID.
5. Veterinario no puede leer un caso no asignado.
6. Transiciones veterinarias válidas funcionan y las inválidas son rechazadas.
7. Admin entra a `admin-home` y puede leer métricas/listas.
8. Ciudadano no puede obtener vistas o permisos institucionales manipulando JavaScript.
9. Perfil inexistente, inactivo o con `activeRole` inválido provoca cierre de sesión.
10. No se añaden permisos GPS al manifest.

## Criterio de terminado

Este bloque Beta está terminado cuando los tres tipos de cuenta pueden iniciar sesión con el mismo APK, cada uno llega a su portal, Firestore aplica permisos por rol, el ciudadano conserva sus flujos Alpha y las pruebas automáticas/regresión pasan.
