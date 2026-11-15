package com.bienestaranimal.zipaquira.web

import java.net.URI

/** Defines the only origin that may execute inside the trusted application WebView. */
object AppNavigationPolicy {
    const val TRUSTED_HOST = "appassets.androidplatform.net"
    const val ROOT_URL = "https://$TRUSTED_HOST/assets/index.html"

    fun isTrusted(url: String): Boolean {
        return try {
            val uri = URI(url)
            val secureScheme = uri.scheme.equals("https", ignoreCase = true)
            val trustedHost = uri.host.equals(TRUSTED_HOST, ignoreCase = true)
            val trustedPort = uri.port == -1 || uri.port == 443
            val trustedPath = uri.rawPath?.startsWith("/assets/") == true
            secureScheme && trustedHost && trustedPort && trustedPath
        } catch (_: Exception) {
            false
        }
    }

    fun isTrustedResource(url: String): Boolean {
        if (isTrusted(url)) return true
        return try {
            val uri = URI(url)
            if (!uri.scheme.equals("https", ignoreCase = true)) return false
            val host = uri.host?.lowercase().orEmpty()
            host == "firebasestorage.googleapis.com" || host == "storage.googleapis.com"
        } catch (_: Exception) {
            false
        }
    }
}
