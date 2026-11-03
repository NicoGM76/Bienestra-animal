package com.bienestaranimal.zipaquira.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationPolicyTest {
    @Test
    fun allowsOnlyTheLocalAssetOrigin() {
        assertTrue(AppNavigationPolicy.isTrusted(AppNavigationPolicy.ROOT_URL))
        assertTrue(AppNavigationPolicy.isTrusted("https://appassets.androidplatform.net/assets/css/styles.css"))
        assertFalse(AppNavigationPolicy.isTrusted("file:///android_asset/index.html"))
        assertFalse(AppNavigationPolicy.isTrusted("http://appassets.androidplatform.net/assets/index.html"))
        assertFalse(AppNavigationPolicy.isTrusted("https://example.com/assets/index.html"))
        assertFalse(AppNavigationPolicy.isTrusted("javascript:alert(1)"))
    }
}
