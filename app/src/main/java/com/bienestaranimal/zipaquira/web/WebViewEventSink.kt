package com.bienestaranimal.zipaquira.web

import android.webkit.WebView
import org.json.JSONObject

class WebViewEventSink(private val webView: WebView) : WebEventSink {
    override fun emit(eventName: String, payloadJson: String) {
        val safeEventName = JSONObject.quote(eventName)
        webView.post {
            webView.evaluateJavascript(
                "window.BAZNative && window.BAZNative.onNativeEvent($safeEventName, $payloadJson);",
                null,
            )
        }
    }
}
