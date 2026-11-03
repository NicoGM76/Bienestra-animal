package com.bienestaranimal.zipaquira.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BridgeMessagePolicyTest {
    @Test
    fun trimsAndAcceptsShortMessages() {
        assertEquals("Hola", BridgeMessagePolicy.sanitize("  Hola  "))
    }

    @Test
    fun rejectsBlankMessages() {
        assertNull(BridgeMessagePolicy.sanitize("   "))
    }

    @Test
    fun rejectsMessagesLongerThan200Characters() {
        assertEquals(200, BridgeMessagePolicy.sanitize("a".repeat(200))?.length)
        assertNull(BridgeMessagePolicy.sanitize("a".repeat(201)))
    }
}
