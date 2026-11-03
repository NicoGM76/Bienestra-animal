# Bienestar Animal Zipaquirá — Demo Final 0.3

Demo Android híbrida para la plataforma de gestión y bienestar animal de Zipaquirá, con enfoque prioritario en zonas rurales.

## Tecnología

- Android: **Kotlin**.
- Interfaz: **HTML + CSS + JavaScript** dentro de WebView segura.
- Backend Demo: **Firebase Authentication + Cloud Firestore**.
- Variante de prueba: `demoDebug` con paquete `com.bienestaranimal.zipaquira.demo`.
- Compatibilidad objetivo: mínimo Android 6 / API 23; prueba antigua principal Android 7 / API 24.

## Funciones reales de esta demo

1. Registro ciudadano con Firebase Authentication.
2. Inicio de sesión real con Firebase Authentication.
3. Creación de perfil `users/{uid}` en Firestore con rol fijo `citizen`.
4. Registro de animales en `animals/{animalId}`.
5. Listado de animales del ciudadano autenticado.
6. Reportes de **pérdida**, **hallazgo** y **presunto maltrato** en `cases/{caseId}`.
7. Listado de los reportes del ciudadano y su estado general.
8. Identidad protegida marcada automáticamente en reportes de presunto maltrato.
9. Catálogo de adopciones **DEMO** con datos ficticios claramente identificados.
10. WebView endurecida para impedir navegación y recursos externos no autorizados.

## Aún no implementado como backend real

- Fotografías, video y audio con Firebase Storage.
- GPS/mapa real.
- Offline con Room + WorkManager.
- Roles institucionales y dashboards de funcionario/veterinario/coordinador/admin.
- IA de priorización y asignación sugerida.
- Backend real de adopciones.
- Notificaciones FCM.

## Antes de ejecutar

1. Abrir Firebase Console y publicar `firestore.rules`.
2. Verificar que Email/Password esté habilitado en Authentication.
3. Abrir el proyecto en Android Studio.
4. Seleccionar `demoDebug`.
5. `Sync Project with Gradle Files`.
6. Ejecutar primero en Android moderno y después en Android 7/API 24.

## Flujo recomendado de sustentación

1. Crear cuenta ciudadana.
2. Mostrar usuario creado en Firebase Authentication y perfil en Firestore.
3. Registrar un animal rural, por ejemplo una cabra.
4. Mostrar el documento en `animals` y el animal en el portal.
5. Enviar un reporte de presunto maltrato.
6. Mostrar que queda `protectedIdentity=true` en Firestore y aparece en “Mis reportes”.
7. Abrir el catálogo de adopciones y aclarar que es interfaz Demo, no backend definitivo.

Equipo: Jorge Luis Osorio Silva y Nicolás García Mejía.

Nota técnica: el flavor activo se expone mediante `BuildConfig.ENVIRONMENT` para distinguir demo/prod.
