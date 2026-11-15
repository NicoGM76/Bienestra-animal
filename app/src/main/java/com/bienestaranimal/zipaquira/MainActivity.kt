package com.bienestaranimal.zipaquira

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import com.bienestaranimal.zipaquira.auth.FirebaseAuthGateway
import com.bienestaranimal.zipaquira.admin.FirebaseAdminGateway
import com.bienestaranimal.zipaquira.animals.FirebaseAnimalGateway
import com.bienestaranimal.zipaquira.cases.FirebaseCaseGateway
import com.bienestaranimal.zipaquira.evidence.FirebaseEvidenceGateway
import com.bienestaranimal.zipaquira.notifications.FirebaseNotificationGateway
import com.bienestaranimal.zipaquira.web.AppNavigationPolicy
import com.bienestaranimal.zipaquira.web.AppWebViewClient
import com.bienestaranimal.zipaquira.web.WebViewEventSink
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var eventSink: WebViewEventSink

    private val evidencePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Algunos proveedores no ofrecen permiso persistente; el URI sigue siendo válido en esta sesión.
            }
            if (::eventSink.isInitialized) {
                eventSink.emit(
                    "evidence:selected",
                    JSONObject().put("uri", uri.toString()).toString(),
                )
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        webView.webViewClient = AppWebViewClient(assetLoader)
        webView.webChromeClient = WebChromeClient()

        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        val authGateway = FirebaseAuthGateway()
        val animalGateway = FirebaseAnimalGateway()
        val caseGateway = FirebaseCaseGateway()
        val adminGateway = FirebaseAdminGateway()
        val notificationGateway = FirebaseNotificationGateway()
        val evidenceGateway = FirebaseEvidenceGateway()
        eventSink = WebViewEventSink(webView)
        webView.addJavascriptInterface(
            WebAppInterface(
                context = this,
                authGateway = authGateway,
                animalGateway = animalGateway,
                caseGateway = caseGateway,
                adminGateway = adminGateway,
                notificationGateway = notificationGateway,
                evidenceGateway = evidenceGateway,
                eventSink = eventSink,
                openEvidencePicker = { evidencePicker.launch(arrayOf("image/*")) },
            ),
            "Android",
        )
        webView.loadUrl(AppNavigationPolicy.ROOT_URL)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface("Android")
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
