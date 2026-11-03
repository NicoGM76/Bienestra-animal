package com.bienestaranimal.zipaquira.auth

data class CitizenProfile(
    val uid: String,
    val name: String,
    val email: String,
    val roles: List<String>,
    val activeRole: String,
    val accountType: String,
    val status: String,
) {
    companion object {
        fun new(uid: String, name: String, email: String): CitizenProfile = CitizenProfile(
            uid = uid,
            name = name.trim(),
            email = email.trim().lowercase(),
            roles = listOf("citizen"),
            activeRole = "citizen",
            accountType = "citizen",
            status = "active",
        )
    }
}
