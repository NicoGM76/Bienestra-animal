package com.bienestaranimal.zipaquira.admin

import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class FirebaseAdminGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AdminGateway {

    override fun loadDashboard(callback: (AdminOutcome) -> Unit) {
        if (auth.currentUser == null) {
            callback(AdminOutcome.Error("Debes iniciar sesión para consultar el panel administrativo."))
            return
        }

        val usersTask = firestore.collection("users").get()
        val animalsTask = firestore.collection("animals").get()
        val casesTask = firestore.collection("cases").get()

        Tasks.whenAllSuccess<QuerySnapshot>(usersTask, animalsTask, casesTask)
            .addOnSuccessListener { snapshots ->
                val usersSnapshot = snapshots[0]
                val animalsSnapshot = snapshots[1]
                val casesSnapshot = snapshots[2]

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
                    .sortedBy { it.name.lowercase() }
                    .take(5)

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
                    .take(5)

                val cases = casesSnapshot.documents
                    .sortedByDescending { it.getTimestamp("createdAt")?.seconds ?: 0L }
                    .take(5)
                    .map { document ->
                        AdminCaseSummary(
                            id = document.id,
                            type = document.getString("type").orEmpty(),
                            status = document.getString("status") ?: "received",
                            finalPriority = document.getString("finalPriority") ?: "pending_review",
                            neighborhoodOrVillage = document.getString("neighborhoodOrVillage").orEmpty(),
                        )
                    }

                callback(
                    AdminOutcome.Loaded(
                        userCount = usersSnapshot.size(),
                        animalCount = animalsSnapshot.size(),
                        caseCount = casesSnapshot.size(),
                        users = users,
                        animals = animals,
                        cases = cases,
                    )
                )
            }
            .addOnFailureListener { error ->
                callback(AdminOutcome.Error(userMessageFor(error)))
            }
    }

    private fun userMessageFor(error: Exception): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
        else -> "No fue posible cargar el panel administrativo."
    }
}
