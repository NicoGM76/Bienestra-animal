# Beta 0.4 — Guía de pre-sustentación

## Qué demuestra esta Beta

La misma APK reconoce tres tipos de cuenta desde Firebase:

1. Ciudadano.
2. Veterinario.
3. Administrador.

El rol se obtiene desde `users/{uid}` y los permisos reales los aplica Firestore Rules.

La aplicación no usa GPS. Toda ubicación es ingresada manualmente.

---

## Preparación previa en Firebase

### 1. Publicar reglas

Copiar y publicar el contenido actual de:

```text
firestore.rules
```

No probar Veterinario/Admin con las reglas Alpha porque Firestore rechazará las consultas institucionales.

### 2. Ciudadano

Usar una cuenta ciudadana ya existente o crear una desde la app.

Perfil esperado:

```text
roles = ["citizen"]
activeRole = "citizen"
accountType = "citizen"
status = "active"
```

### 3. Veterinario

Crear manualmente una cuenta en Firebase Authentication y luego un documento `users/{UID_VET}`:

```text
uid: "<UID_VET>"
name: "Veterinario Demo"
email: "<correo de la cuenta>"
roles: ["veterinarian"]
activeRole: "veterinarian"
accountType: "institutional"
status: "active"
createdAt: timestamp
```

Elegir un caso ciudadano ya existente y agregar:

```text
assignedTo: "<UID_VET>"
assignedRole: "veterinarian"
status: "assigned"
updatedAt: timestamp
```

Conservar todos los demás campos del caso.

### 4. Administrador

Crear manualmente una cuenta en Firebase Authentication y luego `users/{UID_ADMIN}`:

```text
uid: "<UID_ADMIN>"
name: "Administrador Demo"
email: "<correo de la cuenta>"
roles: ["administrator"]
activeRole: "administrator"
accountType: "institutional"
status: "active"
createdAt: timestamp
```

No guardar contraseñas en GitHub ni en este documento.

---

## Orden recomendado de demostración

### Prueba 1 — Ciudadano

1. Iniciar sesión con una cuenta ciudadana.
2. Confirmar que abre **Portal ciudadano**.
3. Mostrar un animal ya registrado.
4. Mostrar un reporte ya registrado.
5. Cerrar sesión.

Resultado esperado: los datos Alpha siguen disponibles.

### Prueba 2 — Veterinario

1. Iniciar sesión con la cuenta Veterinario Demo.
2. Confirmar que abre **Portal veterinario**, no Portal ciudadano.
3. Mostrar únicamente el caso asignado a ese UID.
4. Pulsar **Iniciar atención**.
5. Confirmar en Firestore: `status = "in_progress"`.
6. Pulsar **Marcar como realizado**.
7. Confirmar en Firestore: `status = "completed"`.
8. Cerrar sesión.

Resultado esperado: el veterinario solo trabaja sobre casos asignados a su cuenta.

### Prueba 3 — Administrador

1. Iniciar sesión con la cuenta Administrador Demo.
2. Confirmar que abre **Portal administrador**.
3. Mostrar conteos reales:
   - usuarios;
   - animales;
   - casos.
4. Mostrar las muestras de registros cargadas desde Firestore.
5. Aclarar que en esta Beta el panel es de solo lectura.
6. Cerrar sesión.

Resultado esperado: el administrador consulta información institucional sin modificar datos.

---

## Comprobaciones de seguridad para explicar

- Un ciudadano no puede seleccionar Veterinario o Administrador al registrarse.
- `roles` y `activeRole` no se pueden editar desde el cliente.
- Ocultar o mostrar una pantalla en JavaScript no concede acceso a Firestore.
- Veterinario solo puede leer casos con `assignedTo == su UID`.
- Veterinario únicamente puede modificar `status` y `updatedAt` en las dos transiciones habilitadas.
- Administrador tiene lectura institucional, no edición.
- La eliminación continúa bloqueada.
- Al cerrar sesión se limpian los datos mostrados y la navegación protegida vuelve al login.

---

## Estado conocido que NO debe presentarse como resuelto

La configuración declara `minSdk 23`, pero la compatibilidad completa con dispositivos Android antiguos continúa pendiente. La Beta para pre-sustentación se debe demostrar en el entorno moderno donde la aplicación ya funciona.

---

## Checklist antes de entrar a sustentar

- [ ] Rama local: `beta/roles-institucionales`.
- [ ] `git pull` realizado.
- [ ] Gradle Sync sin errores.
- [ ] Build Variant: `demoDebug`.
- [ ] Reglas Beta publicadas en Firestore.
- [ ] Cuenta Ciudadano probada.
- [ ] Cuenta Veterinario creada y con un caso asignado.
- [ ] Cuenta Administrador creada.
- [ ] Login/logout probado en los tres roles.
- [ ] Animal ciudadano visible.
- [ ] Reporte ciudadano visible.
- [ ] Flujo veterinario `assigned -> in_progress -> completed` probado.
- [ ] Dashboard Admin cargando datos reales.
- [ ] APK Beta generado después de las pruebas.
