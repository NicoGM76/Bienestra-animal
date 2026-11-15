package com.bienestaranimal.zipaquira.push

import com.google.firebase.messaging.FirebaseMessaging

class FirebasePushRegistrationGateway {

    fun registerCurrentDevice() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> PushTokenStore.save(token) }
    }
}
