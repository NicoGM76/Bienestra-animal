# Beta de Roles Institucionales — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar una Beta demostrable para pre-sustentación donde Ciudadano, Veterinario y Administrador inicien sesión con el mismo APK y reciban acceso real según su rol, sin romper los flujos Alpha existentes.

**Architecture:** Se conserva la arquitectura Kotlin + WebView + HTML/CSS/JavaScript + Firebase. El rol se valida en Kotlin a partir de `users/{uid}`; JavaScript solo enruta la UI con el rol ya validado. Firestore Rules aplica la autorización real por rol y mantiene denegación por defecto.

**Tech Stack:** Kotlin, Android WebView, HTML/CSS/JavaScript, Firebase Authentication, Cloud Firestore, Python contract tests, JUnit.

**Spec:** `docs/superpowers/specs/2026-11-03-beta-roles-institucionales-design.md`

## Global Constraints

- Rama de trabajo: `beta/roles-institucionales`; `main` conserva la Alpha estable.
- No usar GPS, geolocalización automática ni permisos de ubicación.
- Mantener `minSdk 23`, `targetSdk 35`, `compileSdk 35`.
- No migraciones destructivas: usuarios, animales y casos Alpha deben seguir siendo legibles.
- No crear cuentas institucionales desde la app en esta Beta.
- El administrador será de solo lectura en este bloque.
- No implementar coordinador, funcionario de campo, IA, multimedia ni offline en este bloque.
- Nada se considera terminado sin regresión de Ciudadano.

## Review Focus

- Perfil inexistente o inactivo: debe cerrar sesión y negar acceso.
- `activeRole` que no pertenece a `roles`: debe rechazarse antes de entrar a cualquier portal.
- Manipulación de JavaScript para abrir `vet-home` o `admin-home`: no debe conceder permisos Firestore.
- Veterinario intentando leer/modificar un caso no asignado: Firestore debe negar.
- Casos Alpha sin `assignedTo`: deben seguir siendo visibles para su ciudadano y para administrador, sin romper consultas.

---

### Task 1: Contrato de autenticación con rol

**Files:**
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/auth/AuthOutcome.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/auth/FirebaseAuthGateway.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt`
- Create: `tests/test_role_auth_contract.py`

**Interfaces:**
- Produces: `AuthOutcome.Success(uid, email, name, activeRole, roles)`
- Produces JS event: `auth:success` with `uid`, `email`, `name`, `activeRole`, `roles`

- [ ] Write a failing contract test requiring role fields, profile lookup, active status validation and role membership validation.
- [ ] Run `python -m unittest tests.test_role_auth_contract -v` and confirm FAIL because role-aware auth is absent.
- [ ] Implement the minimal Kotlin changes so sign-in reads `users/{uid}`, rejects invalid profiles and emits role data.
- [ ] Run the targeted test and then `python -m unittest discover -s tests -v`; all must pass.
- [ ] Commit: `feat: load and validate active user role`

### Task 2: Frontend routing por rol y eliminación de promesa de mapa

**Files:**
- Modify: `app/src/main/assets/js/app.js`
- Modify: `app/src/main/assets/index.html`
- Modify: `app/src/main/assets/css/styles.css`
- Create: `tests/test_role_frontend_contract.py`

**Interfaces:**
- Consumes: `auth:success.activeRole`
- Produces screens: `home`, `vet-home`, `admin-home`

- [ ] Write a failing contract test requiring routing citizen/veterinarian/administrator and absence of the map promise.
- [ ] Run targeted test and confirm FAIL.
- [ ] Implement `openPortalForRole(data)`, add minimal portal shells and remove `Confirmar en mapa · próximo avance`.
- [ ] Run targeted test and full Python suite.
- [ ] Commit: `feat: route authenticated users by role`

### Task 3: Casos asignados para Veterinario

**Files:**
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/cases/CaseGateway.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/cases/FirebaseCaseGateway.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/cases/CaseSummary.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt`
- Modify: `app/src/main/assets/js/app.js`
- Create: `tests/test_veterinarian_case_contract.py`

**Interfaces:**
- Produces: `CaseGateway.listAssignedCases(callback)`
- Produces: `CaseGateway.updateAssignedCaseStatus(caseId, newStatus, callback)`
- Allowed transitions: `assigned -> in_progress`, `in_progress -> completed`
- JS events: `vet:cases`, `vet:case-updated`, `vet:error`

- [ ] Write failing tests for assigned-only query and allowed transition names.
- [ ] Run targeted test and confirm FAIL.
- [ ] Implement minimal gateway, bridge and UI support.
- [ ] Run targeted and full suite.
- [ ] Commit: `feat: add veterinarian assigned case workflow`

### Task 4: Portal Administrador de solo lectura

**Files:**
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/admin/AdminGateway.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/admin/FirebaseAdminGateway.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/admin/AdminOutcome.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/MainActivity.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt`
- Modify: `app/src/main/assets/js/app.js`
- Modify: `app/src/main/assets/index.html`
- Create: `tests/test_admin_portal_contract.py`

**Interfaces:**
- Produces: read-only counts for users, animals and cases.
- Produces: bounded read-only lists for recent users, animals and cases.
- JS event: `admin:dashboard`

- [ ] Write failing contract test requiring real Firestore reads and no write methods.
- [ ] Run targeted test and confirm FAIL.
- [ ] Implement minimal read-only gateway and dashboard rendering.
- [ ] Run targeted and full suite.
- [ ] Commit: `feat: add read-only administrator dashboard`

### Task 5: Firestore Rules por rol

**Files:**
- Modify: `firestore.rules`
- Modify: `tests/test_firestore_rules_contract.py`
- Create: `tests/test_role_firestore_rules_contract.py`
- Modify: `docs/FIREBASE_DEMO_SETUP.md`

**Interfaces:**
- Roles: `citizen`, `veterinarian`, `administrator`
- Veterinarian read condition: `resource.data.assignedTo == request.auth.uid`
- Administrator: read-only institutional access.
- Veterinarian update: only `status` and `updatedAt`, only on assigned case, only allowed transition.

- [ ] Write failing rules contract tests for role helpers, assigned-case restriction and admin read-only behavior.
- [ ] Run targeted tests and confirm FAIL.
- [ ] Implement role helpers and least-privilege rules without weakening citizen rules.
- [ ] Run full suite.
- [ ] Commit: `security: enforce institutional role access`

### Task 6: Regression, presentation data and Beta handoff

**Files:**
- Modify: `README.md`
- Create: `docs/BETA_PRESUSTENTACION.md`

**Interfaces:**
- Test accounts are created manually in Firebase; no passwords stored in repository.
- One veterinarian test account receives at least one manually assigned case.
- One administrator test account is active.

- [ ] Run `python -m unittest discover -s tests -v` and record all passing tests.
- [ ] Run Android/JUnit tests available through Gradle and ensure build compiles for `demoDebug`.
- [ ] Manually verify citizen login, existing animal/report visibility, veterinarian routing/assigned case flow, administrator dashboard, logout for all roles.
- [ ] Document exact Firebase fields for the two institutional demo users and one assigned case, without credentials.
- [ ] Commit: `docs: prepare institutional roles beta for presustentacion`

## Pre-sustentación exit criteria

- Un mismo APK permite iniciar sesión como Ciudadano, Veterinario y Administrador.
- Cada rol aterriza en un portal distinto.
- Ciudadano conserva animales y reportes Alpha.
- Veterinario ve solo casos asignados y puede completar el flujo de estado permitido.
- Administrador ve información real en modo lectura.
- Firestore Rules impide elevación de privilegios y acceso cruzado.
- No hay GPS ni permisos de ubicación.
- Toda la suite automática pasa y `demoDebug` compila.
- La rama Beta permanece separada de `main` hasta después de la pre-sustentación.
