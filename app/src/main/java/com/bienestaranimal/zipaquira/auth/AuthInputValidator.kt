package com.bienestaranimal.zipaquira.auth

object AuthInputValidator {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateLogin(email: String, password: String): String? {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isEmpty()) return "Ingresa tu correo."
        if (!emailRegex.matches(normalizedEmail)) return "Ingresa un correo válido."
        if (password.length < 8) return "La contraseña debe tener mínimo 8 caracteres."
        return null
    }

    fun validateRegistration(name: String, email: String, password: String): String? {
        if (name.trim().isEmpty()) return "Ingresa tu nombre."
        return validateLogin(email, password)
    }
}
