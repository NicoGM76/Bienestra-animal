# Firebase Demo — configuración de la Beta

La variante `demo` usa el proyecto Firebase de pruebas. Las credenciales institucionales no se guardan en Git.

## Configuración base

1. Proyecto Firebase: `bienestar-animal-zipaquira`.
2. App Android: `com.bienestaranimal.zipaquira.demo`.
3. Firebase Authentication: proveedor **Correo electrónico/Contraseña** habilitado.
4. Cloud Firestore activo.
5. Publicar el archivo `firestore.rules` de la rama Beta.

## Archivo local

Debe existir:

```text
app/src/demo/google-services.json
```

El archivo permanece fuera del repositorio mediante `.gitignore`.

## Cuenta ciudadana

Se crea desde la aplicación. El perfil conserva:

```text
roles = ["citizen"]
activeRole = "citizen"
accountType = "citizen"
status = "active"
```

## Cuenta Veterinario de prueba

1. Crear el usuario en Firebase Authentication.
2. Copiar su UID.
3. Crear manualmente `users/{UID}` en Firestore con:

```text
uid: "<UID>"
name: "Veterinario Demo"
email: "<correo usado en Authentication>"
roles: ["veterinarian"]
activeRole: "veterinarian"
accountType: "institutional"
status: "active"
createdAt: timestamp
```

Para asignarle un caso existente, agregar o modificar manualmente en `cases/{caseId}`:

```text
assignedTo: "<UID DEL VETERINARIO>"
assignedRole: "veterinarian"
status: "assigned"
updatedAt: timestamp
```

No borrar los campos existentes del caso.

## Cuenta Administrador de prueba

1. Crear el usuario en Firebase Authentication.
2. Copiar su UID.
3. Crear manualmente `users/{UID}` en Firestore con:

```text
uid: "<UID>"
name: "Administrador Demo"
email: "<correo usado en Authentication>"
roles: ["administrator"]
activeRole: "administrator"
accountType: "institutional"
status: "active"
createdAt: timestamp
```

El administrador de esta Beta es de solo lectura desde la aplicación.

## Verificación en Android Studio

- Seleccionar el Build Variant `demoDebug`.
- Probar en Android moderno como entorno principal de la Beta.
- Android 7 / API 24 permanece como referencia de compatibilidad antigua pendiente de estabilización.
- Firebase Authentication gestiona las identidades y Cloud Firestore almacena `users/{uid}`, animales y casos.

## Comprobaciones mínimas


- Ciudadano entra al portal ciudadano y conserva sus animales/reportes.
- Veterinario entra al portal veterinario y solo consulta sus casos asignados.
- Veterinario puede pasar un caso de `assigned` a `in_progress` y luego a `completed`.
- Administrador entra al portal administrador y carga métricas/listados.
- Una cuenta sin perfil, inactiva o con rol inconsistente no entra a un portal.
- No existe selección de rol institucional en el registro ciudadano.
- El AndroidManifest no solicita permisos GPS.

## Compatibilidad

La Beta declara `minSdk 23`, pero la compatibilidad antigua completa sigue pendiente de diagnóstico. No debe presentarse como resuelta hasta tener ejecución estable y evidencia.
