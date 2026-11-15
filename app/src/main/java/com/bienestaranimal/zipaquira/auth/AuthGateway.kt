package com.bienestaranimal.zipaquira.auth

interface AuthGateway {
    fun restoreSession(callback: (AuthOutcome) -> Unit)
    fun signIn(email: String, password: String, callback: (AuthOutcome) -> Unit)
    fun registerCitizen(name: String, email: String, password: String, callback: (AuthOutcome) -> Unit)
    fun signOut()
}
