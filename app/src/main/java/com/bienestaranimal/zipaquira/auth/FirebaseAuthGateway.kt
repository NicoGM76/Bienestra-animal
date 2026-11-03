package com.bienestaranimal.zipaquira.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAuthGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AuthGateway {

    private val supportedRoles = setOf("citizen", "veterinarian", "administrator")

    override fun signIn(email: String, password: String, callback: (AuthOutcome) -> Unit) {
        val normalizedEmail = email.trim().lowercase()
        auth.signInWithEmailAndPassword(normalizedEmail, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    callback(AuthOutcome.Error("No fue posible iniciar sesión."))
                    return@addOnSuccessListener
                }

                firestore.collection("users").document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            auth.signOut()
                            callback(AuthOutcome.Error("La cuenta no tiene un perfil registrado."))
                            return@addOnSuccessListener
                        }

                        val name = document.getString("name").orEmpty()
                        val activeRole = document.getString("activeRole").orEmpty()
                        val status = document.getString("status").orEmpty()
                        val roles = (document.get("roles") as? List<*>)
                            ?.filterIsInstance<String>()
                            ?: emptyList()

                        if (status != "active") {
                            auth.signOut()
                            callback(AuthOutcome.Error("La cuenta no se encuentra activa."))
                            return@addOnSuccessListener
                        }

                        if (activeRole.isBlank() || activeRole !in roles || activeRole !in supportedRoles) {
                            auth.signOut()
                            callback(AuthOutcome.Error("El perfil de usuario no tiene un rol válido para esta versión."))
                            return@addOnSuccessListener
                        }

                        callback(
                            AuthOutcome.Success(
                                uid = user.uid,
                                email = user.email.orEmpty(),
                                name = name,
                                activeRole = activeRole,
                                roles = roles,
                            )
                        )
                    }
                    .addOnFailureListener {
                        auth.signOut()
                        callback(AuthOutcome.Error("No fue posible cargar el perfil de usuario."))
                    }
            }
            .addOnFailureListener { error ->
                callback(AuthOutcome.Error(userMessageFor(error)))
            }
    }

    override fun registerCitizen(
        name: String,
        email: String,
        password: String,
        callback: (AuthOutcome) -> Unit,
    ) {
        val normalizedEmail = email.trim().lowercase()
        auth.createUserWithEmailAndPassword(normalizedEmail, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    callback(AuthOutcome.Error("No fue posible crear la cuenta."))
                    return@addOnSuccessListener
                }

                val profile = CitizenProfile.new(user.uid, name, normalizedEmail)
                val profileData = mapOf(
                    "uid" to profile.uid,
                    "name" to profile.name,
                    "email" to profile.email,
                    "roles" to profile.roles,
                    "activeRole" to profile.activeRole,
                    "accountType" to profile.accountType,
                    "status" to profile.status,
                    "createdAt" to FieldValue.serverTimestamp(),
                )

                firestore.collection("users").document(user.uid)
                    .set(profileData)
                    .addOnSuccessListener {
                        callback(
                            AuthOutcome.Success(
                                uid = user.uid,
                                email = profile.email,
                                name = profile.name,
                                activeRole = profile.activeRole,
                                roles = profile.roles,
                            )
                        )
                    }
                    .addOnFailureListener {
                        // Evita dejar una cuenta de Auth huérfana cuando falla la creación del perfil.
                        user.delete().addOnCompleteListener {
                            callback(AuthOutcome.Error("No fue posible completar el registro. Intenta de nuevo."))
                        }
                    }
            }
            .addOnFailureListener { error ->
                callback(AuthOutcome.Error(userMessageFor(error)))
            }
    }

    override fun signOut() {
        auth.signOut()
    }

    private fun userMessageFor(error: Exception): String = when (error) {
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con este correo."
        is FirebaseAuthWeakPasswordException -> "La contraseña no cumple los requisitos de seguridad."
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
        is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
        else -> "No fue posible completar la operación. Intenta de nuevo."
    }
}
