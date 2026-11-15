package com.bienestaranimal.zipaquira.push

import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

object PushTokenStore {

    fun save(
        token: String,
        auth: FirebaseAuth = FirebaseAuth.getInstance(),
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    ) {
        val user = auth.currentUser ?: return
        if (token.isBlank()) return

        val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
        val deviceId = Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )

        firestore.collection("users")
            .document(user.uid)
            .collection("devices")
            .document(deviceId)
            .set(
                mapOf(
                    "token" to token,
                    "platform" to "android",
                    "enabled" to true,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
    }
}
