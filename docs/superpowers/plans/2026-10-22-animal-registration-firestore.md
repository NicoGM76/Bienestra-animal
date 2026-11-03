# Animal Registration Firestore Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convertir el registro de animales de simulación a un flujo real: guardar un animal del ciudadano autenticado en Firestore y mostrar sus animales en el portal.

**Architecture:** El frontend WebView recopila datos y los entrega al bridge Kotlin. Kotlin valida y delega a `AnimalGateway`; la implementación Firebase usa `FirebaseAuth.currentUser` como propietario y `animals/{animalId}` en Firestore. El frontend recibe eventos nativos para actualizar la lista sin acceso directo a Firebase.

**Tech Stack:** Kotlin, Android WebView, Firebase Authentication, Cloud Firestore, HTML/CSS/JavaScript.

**Spec:** `docs/superpowers/specs/2026-10-20-bienestar-animal-firebase-backend-design.md` (si está disponible en el repo fuente; decisiones aprobadas en conversación).

## Global Constraints

- `minSdk = 23`; Android 7/API 24 es la prueba antigua principal.
- El frontend nunca decide el UID propietario ni roles.
- Colección Firestore: `animals`.
- Un ciudadano solo puede leer/crear animales cuyo `mainResponsibleId` sea su UID.
- No implementar todavía fotos, mapa/GPS, edición, borrado, offline ni cuidadores adicionales.
- Mantener mensajes y formularios breves.

## Review Focus

- Usuario no autenticado intenta guardar/listar animales: rechazar con mensaje claro.
- Nombre vacío o especie ausente: no escribir en Firestore.
- El UID enviado por el frontend no existe: Kotlin toma siempre `FirebaseAuth.currentUser.uid`.
- Fallo de red/Firestore: el botón vuelve a estado usable y se muestra error.
- Lista vacía: mantener estado vacío accesible; lista con datos: renderizar sin `innerHTML` inseguro.

---

### Task 1: Dominio y validación de animales
**Files:**
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/animals/AnimalDraft.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/animals/AnimalInputValidator.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/animals/AnimalOutcome.kt`
- Create: `tests/kotlin/AnimalDomainCheck.kt`

**Interfaces:**
- Produces: `AnimalDraft`, `AnimalInputValidator.validate(draft): String?`, `AnimalOutcome`.

- [ ] RED: agregar chequeos para nombre obligatorio, especie obligatoria y normalización de campos.
- [ ] Ejecutar chequeo Kotlin y observar fallo.
- [ ] GREEN: implementar modelos/validador mínimos.
- [ ] Ejecutar chequeo y confirmar `ANIMAL_DOMAIN_OK`.
- [ ] Commit.

### Task 2: Gateway Firestore y reglas
**Files:**
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/animals/AnimalGateway.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/animals/FirebaseAnimalGateway.kt`
- Modify: `firestore.rules`
- Create: `tests/test_animal_firestore_contract.py`

**Interfaces:**
- Consumes: `AnimalDraft`, `AnimalOutcome`.
- Produces: `saveAnimal(draft, callback)` y `listMyAnimals(callback)`.

- [ ] RED: probar contrato, propiedad por UID y reglas de Firestore.
- [ ] GREEN: implementar gateway usando UID de Firebase Auth y timestamps del servidor.
- [ ] GREEN: permitir create/read únicamente al responsable principal autenticado.
- [ ] Ejecutar pruebas.
- [ ] Commit.

### Task 3: Bridge Kotlin ↔ JavaScript
**Files:**
- Modify: `WebAppInterface.kt`
- Modify: `MainActivity.kt`
- Create: `tests/test_animal_bridge_contract.py`

**Interfaces:**
- Produces eventos `animals:loading`, `animals:saved`, `animals:list`, `animals:error`.

- [ ] RED: comprobar que el bridge expone `saveAnimal(...)` y `loadMyAnimals()` y no acepta UID.
- [ ] GREEN: conectar `AnimalGateway` al bridge y emitir JSON seguro.
- [ ] Ejecutar pruebas.
- [ ] Commit.

### Task 4: Formulario y listado reales
**Files:**
- Modify: `app/src/main/assets/index.html`
- Modify: `app/src/main/assets/js/app.js`
- Modify: `app/src/main/assets/css/styles.css`
- Create: `tests/test_animal_frontend_contract.py`

**Interfaces:**
- Consumes eventos nativos y métodos del bridge de Task 3.

- [ ] RED: probar IDs del formulario, guardado real, lista dinámica y ausencia de texto “simulado”.
- [ ] GREEN: asignar IDs a campos, serializar formulario, manejar estados de carga/error/éxito.
- [ ] GREEN: renderizar tarjetas con `textContent`/DOM, no HTML crudo.
- [ ] GREEN: recargar lista tras login/registro y tras guardar.
- [ ] Ejecutar suite Python + `node --check`.
- [ ] Commit.

### Task 5: Documentación y paquete de prueba
**Files:**
- Modify: `README.md`
- Modify: `FIREBASE_PRUEBAS.txt`
- Create: `docs/ANIMALES_FIRESTORE.md`

- [ ] Actualizar documentación al estado real.
- [ ] Ejecutar toda la suite local.
- [ ] Empaquetar ZIP para Android Studio.
- [ ] En la otra PC: Sync, publicar reglas, probar crear/listar animal en API 33 y API 24.
