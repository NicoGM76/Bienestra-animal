package com.bienestaranimal.zipaquira

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import com.bienestaranimal.zipaquira.auth.FirebaseAuthGateway
import com.bienestaranimal.zipaquira.animals.FirebaseAnimalGateway
import com.bienestaranimal.zipaquira.cases.FirebaseCaseGateway
import com.bienestaranimal.zipaquira.web.AppNavigationPolicy
import com.bienestaranimal.zipaquira.web.AppWebViewClient
import com.bienestaranimal.zipaquira.web.WebViewEventSink

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

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

        val authGateway = FirebaseAuthGateway()
        val animalGateway = FirebaseAnimalGateway()
        val caseGateway = FirebaseCaseGateway()
        val eventSink = WebViewEventSink(webView)
        webView.addJavascriptInterface(
            WebAppInterface(this, authGateway, animalGateway, caseGateway, eventSink),
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
