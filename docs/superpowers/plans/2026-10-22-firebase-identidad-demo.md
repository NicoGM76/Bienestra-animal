# Firebase Demo + Identidad Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reemplazar el login simulado del flavor `demo` por Firebase Authentication real, permitir autorregistro ciudadano y crear el perfil `users/{uid}` en Cloud Firestore sin habilitar todavía roles institucionales.

**Architecture:** El frontend HTML/JS conserva la UI y llama un puente mínimo JS→Kotlin. Kotlin valida entradas, delega Authentication/Firestore a un gateway dedicado y devuelve eventos estructurados a la WebView. El flavor `demo` usa su `google-services.json`; `prod` permanece sin backend hasta crear el proyecto Firebase de producción.

**Tech Stack:** Kotlin 2.0.21, Android minSdk 23, WebViewAssetLoader, Firebase Android BoM 34.19.0, Google Services Gradle Plugin 4.5.0, Firebase Authentication, Cloud Firestore, HTML/CSS/JavaScript.

**Spec:** `docs/superpowers/specs/2026-10-20-bienestar-animal-firebase-backend-design.md`

## Global Constraints

- `minSdk = 23`; Android 7 / API 24 sigue siendo la prueba antigua principal.
- Ciudadanos pueden autorregistrarse con correo y contraseña.
- Personal institucional NO puede autorregistrarse ni asignarse roles desde la app.
- Todo ciudadano nuevo inicia con `roles=["citizen"]`, `activeRole="citizen"`, `accountType="citizen"`, `status="active"`.
- El frontend nunca decide permisos; `activeRole` es UX, no autorización.
- No incluir claves administrativas ni secretos en HTML/JS/Kotlin.
- No implementar todavía MFA, App Check, roles institucionales, Storage, FCM ni Cloud Functions.
- Mantener separación `demo` / `prod`.

## Review Focus

- Correo vacío/malformado y contraseña menor de 8 caracteres deben rechazarse antes de llamar Firebase.
- Fallo de Auth después de crear cuenta no debe producir un perfil con rol distinto de `citizen`.
- Un usuario no puede enviar desde JS un rol arbitrario durante el registro.
- Errores Firebase deben transformarse en mensajes de UI sin exponer tokens, claves ni stack traces.
- La variante `demo` debe usar el package `com.bienestaranimal.zipaquira.demo`; `prod` no debe reutilizar su configuración Firebase.

---

### Task 1: Cablear Firebase solo para Demo

**Files:**
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`
- Create: `app/src/demo/google-services.json`
- Create: `tests/test_firebase_demo_config.py`

**Interfaces:**
- Consumes: flavor `demo` existente con `applicationIdSuffix = ".demo"`.
- Produces: Gradle preparado para `firebase-auth` y `firebase-firestore` usando BoM; configuración Firebase presente solo en `app/src/demo/`.

- [ ] **Step 1: Write the failing configuration test**

Crear `tests/test_firebase_demo_config.py` que compruebe: plugin Google Services 4.5.0 en raíz; plugin aplicado en `app`; BoM 34.19.0; dependencias `firebase-auth` y `firebase-firestore`; JSON bajo `app/src/demo`; `package_name == com.bienestaranimal.zipaquira.demo`; ausencia de `app/src/prod/google-services.json`.

- [ ] **Step 2: Run test to verify it fails**

Run: `python -m unittest tests.test_firebase_demo_config -v`
Expected: FAIL porque Firebase todavía no está cableado.

- [ ] **Step 3: Add minimal Gradle/Firebase demo wiring**

Usar el JSON proporcionado por el usuario sin modificar sus identificadores. No imprimir ni copiar su API key en documentación o logs.

- [ ] **Step 4: Run test to verify it passes**

Run: `python -m unittest tests.test_firebase_demo_config -v`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add build.gradle.kts app/build.gradle.kts app/src/demo/google-services.json tests/test_firebase_demo_config.py
git commit -m "build: wire firebase demo environment"
```

### Task 2: Dominio puro de autenticación ciudadana

**Files:**
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/auth/AuthInputValidator.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/auth/AuthOutcome.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/auth/CitizenProfile.kt`
- Create: `tests/kotlin/AuthDomainCheck.kt`

**Interfaces:**
- Produces: `AuthInputValidator.validateLogin(email: String, password: String): String?`; `validateRegistration(name: String, email: String, password: String): String?`; `CitizenProfile.new(uid: String, name: String, email: String): CitizenProfile`; `AuthOutcome` Success/Error.

- [ ] **Step 1: Write the failing pure-Kotlin check**

El check debe afirmar: email válido aceptado; email inválido rechazado; contraseña <8 rechazada; nombre vacío rechazado en registro; `CitizenProfile.new` fija exclusivamente rol/activeRole/accountType/status de ciudadano.

- [ ] **Step 2: Compile/run and verify failure**

Run: `kotlinc tests/kotlin/AuthDomainCheck.kt app/src/main/java/com/bienestaranimal/zipaquira/auth/*.kt -include-runtime -d /tmp/auth-domain.jar && java -jar /tmp/auth-domain.jar`
Expected: FAIL antes de crear las clases.

- [ ] **Step 3: Implement minimal pure-Kotlin domain**

No depender de Android/Firebase en estas clases.

- [ ] **Step 4: Compile/run and verify pass**

Run: same command.
Expected: output `AUTH_DOMAIN_OK` and exit 0.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/bienestaranimal/zipaquira/auth tests/kotlin/AuthDomainCheck.kt
git commit -m "feat: add citizen auth domain"
```

### Task 3: Firebase Auth + creación de `users/{uid}`

**Files:**
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/auth/AuthGateway.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/auth/FirebaseAuthGateway.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/web/WebEventSink.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/web/WebViewEventSink.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt`
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/MainActivity.kt`
- Create: `tests/test_auth_bridge_contract.py`

**Interfaces:**
- `AuthGateway.signIn(email: String, password: String, callback: (AuthOutcome) -> Unit)`.
- `AuthGateway.registerCitizen(name: String, email: String, password: String, callback: (AuthOutcome) -> Unit)`.
- `WebEventSink.emit(eventName: String, payloadJson: String)`.
- JS bridge exposes only `login(email,password)` and `registerCitizen(name,email,password)` for this slice; no role parameter.

- [ ] **Step 1: Write failing bridge contract test**

Static contract checks exact public bridge methods, absence of any role argument/setRole method, Firestore path `users/{uid}`, and creation fields required by spec.

- [ ] **Step 2: Run and verify failure**

Run: `python -m unittest tests.test_auth_bridge_contract -v`
Expected: FAIL porque gateway/bridge aún no existen.

- [ ] **Step 3: Implement Firebase gateway and WebView event sink**

Registration sequence: Firebase Auth account → build immutable citizen profile → `users/{uid}` Firestore document. On profile write failure, return error and do not report registration success. Do not expose exception internals to JS.

- [ ] **Step 4: Run static contract test**

Run: `python -m unittest tests.test_auth_bridge_contract -v`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/bienestaranimal/zipaquira tests/test_auth_bridge_contract.py
git commit -m "feat: connect firebase citizen authentication"
```

### Task 4: Reemplazar login simulado y añadir registro ciudadano

**Files:**
- Modify: `app/src/main/assets/index.html`
- Modify: `app/src/main/assets/js/app.js`
- Modify: `app/src/main/assets/css/styles.css`
- Create: `tests/test_auth_frontend_contract.py`

**Interfaces:**
- JS→Kotlin: `Android.login(email,password)`, `Android.registerCitizen(name,email,password)`.
- Kotlin→JS: `window.BAZNative.onNativeEvent(eventName, payload)`.
- Eventos mínimos: `auth:loading`, `auth:success`, `auth:registered`, `auth:error`.

- [ ] **Step 1: Write failing frontend contract test**

Comprobar que ya no existe el texto/flujo `Ingreso simulado`; que existe pantalla/formulario de registro; que JS no navega a home antes de recibir `auth:success`; que botones se bloquean durante `auth:loading`; y que errores aparecen en UI.

- [ ] **Step 2: Run and verify failure**

Run: `python -m unittest tests.test_auth_frontend_contract -v`
Expected: FAIL contra la UI actual.

- [ ] **Step 3: Implement login/register UI against native bridge**

Mantener formularios cortos y accesibles. No añadir selección de roles al autorregistro.

- [ ] **Step 4: Run JS syntax and frontend contract tests**

Run: `node --check app/src/main/assets/js/app.js && python -m unittest tests.test_auth_frontend_contract -v`
Expected: PASS.

- [ ] **Step 5: Run full local non-Android suite**

Run: `python -m unittest discover -s tests -v`
Expected: all Python checks PASS; ejecutar además el check Kotlin de Task 2.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/assets tests/test_auth_frontend_contract.py
git commit -m "feat: add real citizen login and registration UI"
```

### Task 5: Reglas iniciales y paquete de verificación para la otra PC

**Files:**
- Create: `firestore.rules`
- Create: `firebase.json`
- Create: `docs/FIREBASE_DEMO_SETUP.md`
- Create: `tests/test_firestore_rules_contract.py`

**Interfaces:**
- Usuario autenticado puede crear/leer únicamente su propio `users/{uid}` en esta primera rebanada.
- Un ciudadano no puede crearse con roles institucionales ni modificar `roles`, `activeRole`, `accountType` o `status` para elevar privilegios.

- [ ] **Step 1: Write failing rules contract test**

Comprobar existencia de reglas deny-by-default y restricciones explícitas de `users/{uid}`.

- [ ] **Step 2: Run and verify failure**

Run: `python -m unittest tests.test_firestore_rules_contract -v`
Expected: FAIL antes de crear reglas.

- [ ] **Step 3: Add minimal Firestore rules and setup documentation**

Documentar como pendientes de la otra PC: Gradle Sync, `demoDebug`, Firebase Emulator Suite para rules/auth/firestore, registro/login real, documento `users/{uid}`, Android 7/API24 y Android moderno.

- [ ] **Step 4: Run local suite**

Run: `python -m unittest discover -s tests -v`
Expected: all local tests PASS.

- [ ] **Step 5: Commit**

```bash
git add firestore.rules firebase.json docs/FIREBASE_DEMO_SETUP.md tests/test_firestore_rules_contract.py
git commit -m "security: add initial citizen firestore rules"
```
