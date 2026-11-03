package com.bienestaranimal.zipaquira.auth

sealed interface AuthOutcome {
    data class Success(val uid: String, val email: String) : AuthOutcome
    data class Error(val message: String) : AuthOutcome
}
