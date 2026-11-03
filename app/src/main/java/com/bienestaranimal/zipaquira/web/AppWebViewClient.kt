package com.bienestaranimal.zipaquira.web

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream

class AppWebViewClient(
    private val assetLoader: WebViewAssetLoader
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest
    ): WebResourceResponse? {
        if (!AppNavigationPolicy.isTrusted(request.url.toString())) {
            return blockedResponse()
        }
        return assetLoader.shouldInterceptRequest(request.url)
    }

    @Deprecated("Deprecated in Android SDK; retained for API 23 compatibility")
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        if (url == null || !AppNavigationPolicy.isTrusted(url)) {
            return blockedResponse()
        }
        return assetLoader.shouldInterceptRequest(Uri.parse(url))
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        return !AppNavigationPolicy.isTrusted(request.url.toString())
    }

    @Deprecated("Deprecated in Android SDK; retained for API 23 compatibility")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return url == null || !AppNavigationPolicy.isTrusted(url)
    }

    private fun blockedResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
