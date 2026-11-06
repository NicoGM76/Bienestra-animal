# Bienestar Animal Zipaquirá — Beta 0.4.1

Aplicación Android híbrida para la gestión y bienestar animal del municipio de Zipaquirá, con enfoque prioritario en zonas rurales.

## Tecnología actual

- Android en **Kotlin**.
- Interfaz **HTML + CSS + JavaScript** dentro de WebView.
- **Firebase Authentication** para identidad.
- **Cloud Firestore** para perfiles, animales y casos.
- Variante de prueba: `demoDebug` con paquete `com.bienestaranimal.zipaquira.demo`.
- `versionCode = 5` y `versionName = 0.4.1-beta`.
- El flavor activo se expone mediante `BuildConfig.ENVIRONMENT` para distinguir `demo` y `prod`.
- La aplicación **no usa GPS**. La ubicación se registra manualmente mediante municipio, barrio/vereda/sector, finca/predio/dirección y punto de referencia.

## Funciones reales disponibles

### Ciudadano
- Registro de cuenta ciudadana.
- Inicio y cierre de sesión.
- Perfil real en `users/{uid}`.
- Registro y consulta de animales.
- Reportes de pérdida, hallazgo y presunto maltrato.
- Consulta del estado de reportes.
- Identidad protegida en reportes de presunto maltrato.

### Veterinario
- Inicio de sesión con cuenta institucional.
- Portal propio.
- Consulta de casos asignados a su UID.
- Cambio controlado de estado:
  - `assigned -> in_progress`
  - `in_progress -> completed`
- Resumen de carga: asignados, en atención y finalizados.
- Ficha de atención clínica por caso en curso:
  - diagnóstico o impresión clínica;
  - observaciones;
  - tratamiento o conducta;
  - recomendaciones y seguimiento.
- La ficha queda asociada al caso en Firestore y solo puede editarla el veterinario asignado mientras el caso está `in_progress`.
- No recibe en la interfaz la identidad del ciudadano denunciante.

### Administrador
- Inicio de sesión con cuenta institucional.
- Portal propio de solo lectura.
- Conteo real de usuarios, animales y casos.
- Indicadores de cuentas por rol: ciudadanos, veterinarios y administradores.
- Seguimiento de casos por estado: recibidos, asignados, en atención y finalizados.
- Actividad institucional con cuentas, animales y casos recientes.
- Identificación del profesional asignado a cada caso cuando existe.
- Vista de solo lectura: no modifica registros desde la aplicación.

## Seguridad de roles

El rol no lo decide JavaScript. Después de autenticar, Kotlin consulta `users/{uid}` en Firestore y valida:
- `status = active`;
- `activeRole`;
- que `activeRole` pertenezca a `roles`;
- que el rol esté habilitado en esta Beta.

Firestore Rules aplica los permisos reales:
- ciudadano: sus animales y sus casos;
- veterinario: únicamente casos cuyo `assignedTo` sea su UID;
- administrador: lectura institucional;
- eliminación bloqueada;
- edición de roles desde el cliente bloqueada.

## Estado de compatibilidad

La configuración declara `minSdk 23`. El funcionamiento moderno ya fue probado. La compatibilidad completa con Android antiguo continúa como incidencia conocida y no se presenta como resuelta.

## Antes de probar la Beta

1. Ejecutar `git pull` en la rama `beta/roles-institucionales`.
2. Abrir Android Studio y sincronizar Gradle.
3. Publicar el contenido actualizado de `firestore.rules`.
4. Crear manualmente en Firebase las cuentas institucionales de prueba siguiendo `docs/BETA_PRESUSTENTACION.md`.
5. Seleccionar `demoDebug`.
6. Probar primero los flujos ciudadanos ya existentes.
7. Probar Veterinario.
8. Probar Administrador.

## Funciones que no forman parte de esta Beta

- Creación de cuentas institucionales desde la app.
- Coordinador y funcionario de campo.
- Backend real de adopciones.
- Evidencias multimedia.
- Offline con Room/WorkManager.
- IA de priorización.
- Notificaciones push.

Equipo: Jorge Luis Osorio Silva y Nicolás García Mejía.
