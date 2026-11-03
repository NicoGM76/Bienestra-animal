# Etapa 1 — Fundación Android y WebView segura Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convertir el Avance 1 en una base Android híbrida segura, separada por entorno y compatible desde Android 6, lista para integrar Firebase en la Etapa 2.

**Architecture:** Mantener HTML/CSS/JS dentro de WebView, pero servir assets con `WebViewAssetLoader`, restringir navegación, reducir superficie del `JavascriptInterface` y separar configuración demo/producción. La compatibilidad mínima será API 23 y se validará en una matriz API 23, 24/25 y 36.

**Tech Stack:** Kotlin, AndroidX AppCompat, AndroidX WebKit 1.15.0, WebView, JUnit, Gradle Kotlin DSL.

**Spec:** `docs/superpowers/specs/2026-10-20-bienestar-animal-firebase-backend-design.md`

## Global Constraints

- `minSdk = 23` (Android 6.0).
- Matriz mínima: API 23, API 24/25 y API 36; API 37 beta es prueba adicional.
- Fijar `androidx.webkit:webkit:1.15.0`; no usar 1.16+ mientras se mantenga API 23.
- El frontend sigue siendo HTML/CSS/JS dentro de WebView.
- No agregar Firebase todavía en esta etapa.
- No almacenar secretos en HTML, JavaScript ni Kotlin.
- Mantener funcionamiento en equipos modestos y red limitada.

## Review Focus

- URL externa o `file://` no debe cargarse dentro del WebView de confianza.
- Mensajes vacíos o excesivos enviados por JavaScript no deben producir comportamiento inseguro.
- API 23 debe compilar sin depender de APIs introducidas en API 24+.
- La variante demo debe identificarse claramente y no confundirse con producción.
- El proyecto debe seguir arrancando y navegando con los assets locales después de reemplazar `file:///android_asset`.

---

### Task 1: Compatibilidad base y variantes demo/producción

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/demo/res/values/strings.xml`
- Create: `app/src/prod/res/values/strings.xml`

**Interfaces:**
- Produces: `BuildConfig.ENVIRONMENT`, recursos `app_name` y `environment_label` por flavor.

- [ ] **Step 1:** Cambiar `minSdk` a 23 y añadir flavor dimension `environment` con `demo` y `prod`.
- [ ] **Step 2:** Fijar `androidx.webkit:webkit:1.15.0` y dependencia JUnit para tests JVM.
- [ ] **Step 3:** Crear recursos de nombre/etiqueta por entorno.
- [ ] **Step 4:** Ejecutar `./gradlew :app:testDemoDebugUnitTest :app:assembleDemoDebug` y verificar BUILD SUCCESSFUL.
- [ ] **Step 5:** Commit `build: add environment flavors and api 23 support`.

### Task 2: Política de navegación segura del WebView

**Files:**
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/web/AppNavigationPolicy.kt`
- Create: `app/src/test/java/com/bienestaranimal/zipaquira/web/AppNavigationPolicyTest.kt`

**Interfaces:**
- Produces: `AppNavigationPolicy.isTrusted(uri: Uri): Boolean`.

- [ ] **Step 1:** Escribir tests fallidos para permitir solo `https://appassets.androidplatform.net/assets/...` y rechazar `file://`, `http://`, hosts externos y esquemas desconocidos.
- [ ] **Step 2:** Ejecutar el test y confirmar RED por clase inexistente.
- [ ] **Step 3:** Implementar la política mínima.
- [ ] **Step 4:** Ejecutar test y suite JVM; confirmar GREEN.
- [ ] **Step 5:** Commit `test: define trusted webview navigation policy`.

### Task 3: Carga segura de assets

**Files:**
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/MainActivity.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/web/AppWebViewClient.kt`
- Test: `app/src/test/java/com/bienestaranimal/zipaquira/web/AppNavigationPolicyTest.kt`

**Interfaces:**
- Consumes: `AppNavigationPolicy.isTrusted`.
- Produces: assets servidos por `https://appassets.androidplatform.net/assets/index.html`.

- [ ] **Step 1:** Ampliar el test para fijar la URL raíz confiable.
- [ ] **Step 2:** Verificar RED para el nuevo comportamiento esperado.
- [ ] **Step 3:** Implementar `WebViewAssetLoader`, desactivar file/content access y bloquear navegación no confiable.
- [ ] **Step 4:** Ejecutar tests y `assembleDemoDebug`.
- [ ] **Step 5:** Commit `feat: load local web assets through secure origin`.

### Task 4: Puente JavaScript ↔ Kotlin mínimo y validado

**Files:**
- Modify: `app/src/main/java/com/bienestaranimal/zipaquira/WebAppInterface.kt`
- Create: `app/src/main/java/com/bienestaranimal/zipaquira/web/BridgeMessagePolicy.kt`
- Create: `app/src/test/java/com/bienestaranimal/zipaquira/web/BridgeMessagePolicyTest.kt`

**Interfaces:**
- Produces: `BridgeMessagePolicy.sanitize(message: String): String?` y bridge con API mínima `showToast`, `getPlatform`, `getEnvironment`.

- [ ] **Step 1:** Escribir tests fallidos: trim, rechazo vacío y límite 200 caracteres.
- [ ] **Step 2:** Verificar RED.
- [ ] **Step 3:** Implementar sanitización y aplicarla a `showToast`; exponer entorno de solo lectura.
- [ ] **Step 4:** Ejecutar suite JVM y ensamblado demo.
- [ ] **Step 5:** Commit `feat: harden javascript bridge inputs`.

### Task 5: Manifiesto, red y cierre de compatibilidad

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/xml/network_security_config.xml`
- Create: `docs/COMPATIBILIDAD_ANDROID.md`
- Modify: `README.md`

**Interfaces:**
- Produces: política de red sin cleartext y matriz documentada de prueba.

- [ ] **Step 1:** Configurar `usesCleartextTraffic=false`, `networkSecurityConfig` y nombre por recurso.
- [ ] **Step 2:** Documentar matriz API 23, 24/25, 36 y API 37 beta opcional, incluyendo red lenta/offline y almacenamiento limitado.
- [ ] **Step 3:** Ejecutar `./gradlew :app:testDemoDebugUnitTest :app:assembleDemoDebug :app:assembleProdDebug`.
- [ ] **Step 4:** Inspeccionar APKs generados y confirmar variantes separadas.
- [ ] **Step 5:** Commit `chore: document android compatibility and network policy`.
