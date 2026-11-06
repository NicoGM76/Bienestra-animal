package com.bienestaranimal.zipaquira.admin

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAdminGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AdminGateway {

    override fun loadDashboard(callback: (AdminOutcome) -> Unit) {
        if (auth.currentUser == null) {
            callback(AdminOutcome.Error("Debes iniciar sesión para consultar el panel administrativo."))
            return
        }

        firestore.collection("users").get()
            .addOnSuccessListener { usersSnapshot ->
                firestore.collection("animals").get()
                    .addOnSuccessListener { animalsSnapshot ->
                        firestore.collection("cases").get()
                            .addOnSuccessListener { casesSnapshot ->
                                val userNames = usersSnapshot.documents.associate { document ->
                                    document.id to (document.getString("name").orEmpty().ifBlank { "Cuenta institucional" })
                                }

                                val citizenCount = usersSnapshot.documents.count {
                                    it.getString("activeRole") == "citizen"
                                }
                                val veterinarianCount = usersSnapshot.documents.count {
                                    it.getString("activeRole") == "veterinarian"
                                }
                                val administratorCount = usersSnapshot.documents.count {
                                    it.getString("activeRole") == "administrator"
                                }

                                val receivedCaseCount = casesSnapshot.documents.count {
                                    (it.getString("status") ?: "received") == "received"
                                }
                                val assignedCaseCount = casesSnapshot.documents.count {
                                    it.getString("status") == "assigned"
                                }
                                val inProgressCaseCount = casesSnapshot.documents.count {
                                    it.getString("status") == "in_progress"
                                }
                                val completedCaseCount = casesSnapshot.documents.count {
                                    it.getString("status") == "completed"
                                }

                                val users = usersSnapshot.documents
                                    .map { document ->
                                        AdminUserSummary(
                                            uid = document.id,
                                            name = document.getString("name").orEmpty(),
                                            email = document.getString("email").orEmpty(),
                                            activeRole = document.getString("activeRole").orEmpty(),
                                            status = document.getString("status").orEmpty(),
                                        )
                                    }
                                    .sortedWith(
                                        compareBy<AdminUserSummary> {
                                            when (it.activeRole) {
                                                "administrator" -> 0
                                                "veterinarian" -> 1
                                                else -> 2
                                            }
                                        }.thenBy { it.name.lowercase() }
                                    )
                                    .take(8)

                                val animals = animalsSnapshot.documents
                                    .map { document ->
                                        AdminAnimalSummary(
                                            id = document.id,
                                            name = document.getString("name").orEmpty(),
                                            species = document.getString("species").orEmpty(),
                                            status = document.getString("status") ?: "active",
                                        )
                                    }
                                    .sortedBy { it.name.lowercase() }
                                    .take(8)

                                val cases = casesSnapshot.documents
                                    .sortedByDescending { it.getTimestamp("createdAt")?.seconds ?: 0L }
                                    .take(8)
                                    .map { document ->
                                        val assignedTo = document.getString("assignedTo").orEmpty()
                                        AdminCaseSummary(
                                            id = document.id,
                                            type = document.getString("type").orEmpty(),
                                            status = document.getString("status") ?: "received",
                                            finalPriority = document.getString("finalPriority") ?: "pending_review",
                                            neighborhoodOrVillage = document.getString("neighborhoodOrVillage").orEmpty(),
                                            animalCondition = document.getString("animalCondition").orEmpty(),
                                            assignedToName = if (assignedTo.isBlank()) {
                                                "Sin asignar"
                                            } else {
                                                userNames[assignedTo] ?: "Cuenta institucional"
                                            },
                                        )
                                    }

                                callback(
                                    AdminOutcome.Loaded(
                                        userCount = usersSnapshot.size(),
                                        animalCount = animalsSnapshot.size(),
                                        caseCount = casesSnapshot.size(),
                                        activeCaseCount = assignedCaseCount + inProgressCaseCount,
                                        citizenCount = citizenCount,
                                        veterinarianCount = veterinarianCount,
                                        administratorCount = administratorCount,
                                        receivedCaseCount = receivedCaseCount,
                                        assignedCaseCount = assignedCaseCount,
                                        inProgressCaseCount = inProgressCaseCount,
                                        completedCaseCount = completedCaseCount,
                                        users = users,
                                        animals = animals,
                                        cases = cases,
                                    )
                                )
                            }
                            .addOnFailureListener { callback(AdminOutcome.Error(userMessageFor(it))) }
                    }
                    .addOnFailureListener { callback(AdminOutcome.Error(userMessageFor(it))) }
            }
            .addOnFailureListener { callback(AdminOutcome.Error(userMessageFor(it))) }
    }

    private fun userMessageFor(error: Exception): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
        else -> "No fue posible cargar el panel administrativo."
    }
}
