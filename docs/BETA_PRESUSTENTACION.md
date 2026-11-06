# Beta 0.4.2 — Guía de pre-sustentación

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

No es necesario asignar el caso manualmente desde Firestore. La asignación se realiza desde el portal Administrador durante la demostración.

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

### Prueba 2 — Administrador asigna el caso

1. Iniciar sesión con la cuenta Administrador Demo.
2. Confirmar que abre **Portal administrador**.
3. Mostrar el resumen general, cuentas por rol y seguimiento de casos.
4. Ir a **Asignación de casos**.
5. Seleccionar un caso en estado **Recibido**.
6. Seleccionar el Veterinario Demo.
7. Pulsar **Asignar caso**.
8. Confirmar que el caso desaparece de pendientes y pasa a **Asignado**.
9. Si se desea mostrar evidencia técnica, confirmar en Firestore:
   - `status = "assigned"`;
   - `assignedTo = UID del veterinario`;
   - `assignedRole = "veterinarian"`;
   - `assignedBy = UID del administrador`;
   - `assignedAt = timestamp`.
10. Cerrar sesión.

Resultado esperado: el administrador puede realizar únicamente la asignación operativa del caso, sin editar libremente sus datos.

### Prueba 3 — Veterinario atiende el caso

1. Iniciar sesión con la cuenta Veterinario Demo.
2. Confirmar que abre **Portal veterinario**, no Portal ciudadano.
3. Mostrar únicamente el caso que acaba de asignar el Administrador.
4. Mostrar el resumen de carga: asignados, en atención y finalizados.
5. Pulsar **Iniciar atención**.
6. Confirmar en Firestore: `status = "in_progress"`.
7. Abrir **Ficha de atención**.
8. Registrar diagnóstico, observaciones, tratamiento y recomendaciones.
9. Guardar la ficha y confirmar que los campos veterinarios aparecen en el caso de Firestore.
10. Volver al portal y pulsar **Marcar como realizado**.
11. Confirmar en Firestore: `status = "completed"`.
12. Abrir nuevamente la ficha para mostrarla en modo consulta.
13. Cerrar sesión.

Resultado esperado: el flujo queda conectado de extremo a extremo: Ciudadano reporta -> Administrador asigna -> Veterinario atiende y finaliza.

---

## Comprobaciones de seguridad para explicar

- Un ciudadano no puede seleccionar Veterinario o Administrador al registrarse.
- `roles` y `activeRole` no se pueden editar desde el cliente.
- Ocultar o mostrar una pantalla en JavaScript no concede acceso a Firestore.
- Veterinario solo puede leer casos con `assignedTo == su UID`.
- Veterinario puede modificar `status` y `updatedAt` únicamente en las dos transiciones habilitadas.
- La ficha clínica solo puede guardarla el veterinario asignado cuando el caso está `in_progress`.
- La escritura clínica se limita a diagnóstico, observaciones, tratamiento, recomendaciones y metadatos de auditoría.
- Administrador tiene lectura institucional y solo puede modificar los campos estrictamente necesarios para asignar un caso `received` a un veterinario activo.
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
- [ ] Cuenta Veterinario creada y activa.
- [ ] Asignación Admin `received -> assigned` probada desde la app.
- [ ] Cuenta Administrador creada.
- [ ] Login/logout probado en los tres roles.
- [ ] Animal ciudadano visible.
- [ ] Reporte ciudadano visible.
- [ ] Flujo veterinario `assigned -> in_progress -> completed` probado.
- [ ] Ficha clínica veterinaria guardada y recuperada desde Firestore.
- [ ] Dashboard Admin mostrando roles y estados de casos con datos reales.
- [ ] APK Beta generado después de las pruebas.
