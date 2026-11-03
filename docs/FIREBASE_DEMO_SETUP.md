# Firebase Demo - verificación en Android Studio

Este proyecto usa Firebase **solo en el flavor `demo`**. Producción permanece sin credenciales hasta crear el proyecto institucional correspondiente.

## Configuración que debe existir en Firebase Console

1. Proyecto: `bienestar-animal-zipaquira`.
2. App Android Demo: `com.bienestaranimal.zipaquira.demo`.
3. En **Firebase Authentication**, habilitar el proveedor **Correo electrónico/Contraseña**.
4. Crear **Cloud Firestore** para el proyecto Demo.
5. Publicar el contenido de `firestore.rules` antes de usar datos reales de prueba.

## Archivo local

El archivo de configuración Demo debe existir únicamente en:

```text
app/src/demo/google-services.json
```

No crear `app/src/prod/google-services.json` con las credenciales Demo.

## Nota sobre Gradle Wrapper

Esta copia conserva `gradle/wrapper/gradle-wrapper.properties`, pero el entorno donde se preparó no pudo descargar `gradlew`, `gradlew.bat` ni `gradle-wrapper.jar`. Android Studio puede sincronizar el proyecto si resuelve Gradle desde el IDE; si solicita el Wrapper completo, hay que generarlo/copiarlos una sola vez en la PC con Android Studio antes de las pruebas. No es un cambio de lógica de la app.

## Verificación en la otra PC

1. Abrir la raíz del proyecto en Android Studio.
2. Esperar **Gradle Sync** y confirmar que no haya errores.
3. En **Build Variants**, seleccionar `demoDebug`.
4. Ejecutar primero en **Android 7 / API 24** y después en un Android moderno.
5. Crear una cuenta ciudadana desde **Crear cuenta ciudadana**.
6. Confirmar en Firebase Authentication que aparece el usuario.
7. Confirmar en Cloud Firestore que aparece `users/{uid}` con:
   - `roles = ["citizen"]`
   - `activeRole = "citizen"`
   - `accountType = "citizen"`
   - `status = "active"`
8. Cerrar/reabrir la app e iniciar sesión con la cuenta creada.
9. Probar correo inválido, contraseña menor de 8 caracteres y contraseña incorrecta.
10. Confirmar que la aplicación no ofrece selección de roles institucionales durante el registro.

## Pruebas de reglas

Cuando Firebase CLI esté disponible, ejecutar Firebase Emulator Suite para **Firebase Authentication** y **Cloud Firestore** antes de desplegar cambios de reglas. La prueba con emuladores queda pendiente del equipo que tenga Android SDK/Firebase CLI disponible.

## Qué debe considerarse aprobado

- `demoDebug` compila.
- El APK abre en Android 7/API 24 y Android moderno.
- Registro ciudadano crea Auth + `users/{uid}`.
- Login correcto entra al portal.
- Credenciales incorrectas muestran error y no navegan al portal.
- Ningún usuario puede autorregistrarse como funcionario, veterinario, coordinador o administrador.
