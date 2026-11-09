package com.bienestaranimal.zipaquira.admin

import com.bienestaranimal.zipaquira.cases.CaseCompatibility
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
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
                                    document.id to (
                                        document.getString("name").orEmpty()
                                            .ifBlank { document.getString("email").orEmpty() }
                                            .ifBlank { "Cuenta institucional" }
                                        )
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

                                val normalizedStatuses = casesSnapshot.documents.map(CaseCompatibility::status)
                                val receivedCaseCount = normalizedStatuses.count { it == "received" }
                                val assignedCaseCount = normalizedStatuses.count { it == "assigned" }
                                val inProgressCaseCount = normalizedStatuses.count { it == "in_progress" }
                                val completedCaseCount = normalizedStatuses.count { it == "completed" }

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

                                val cases = casesSnapshot.documents
                                    .sortedByDescending { it.getTimestamp("createdAt")?.seconds ?: 0L }
                                    .map { document ->
                                        toAdminCaseSummary(document, userNames)
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

    override fun assignCase(
        caseId: String,
        veterinarianUid: String,
        callback: (AdminOutcome) -> Unit,
    ) {
        val adminUid = auth.currentUser?.uid
        if (adminUid == null) {
            callback(AdminOutcome.Error("Debes iniciar sesión como administrador para asignar casos."))
            return
        }

        if (caseId.isBlank() || veterinarianUid.isBlank()) {
            callback(AdminOutcome.Error("Selecciona un caso y un veterinario válido."))
            return
        }

        val caseRef = firestore.collection("cases").document(caseId.trim())
        val veterinarianRef = firestore.collection("users").document(veterinarianUid.trim())
        val notificationRef = firestore.collection("notifications").document()

        firestore.runTransaction { transaction ->
            val caseSnapshot = transaction.get(caseRef)
            val veterinarianSnapshot = transaction.get(veterinarianRef)

            if (!caseSnapshot.exists()) {
                throw IllegalStateException("El caso ya no existe.")
            }

            validateVeterinarian(veterinarianSnapshot)

            val currentStatus = CaseCompatibility.status(caseSnapshot)
            if (currentStatus != "received") {
                throw IllegalStateException("Solo se pueden asignar casos que estén en estado recibido.")
            }

            val veterinarianName = veterinarianSnapshot.getString("name").orEmpty().ifBlank { "Veterinario" }

            transaction.update(
                caseRef,
                mapOf(
                    "schemaVersion" to 2,
                    "assignedTo" to veterinarianUid.trim(),
                    "assignedRole" to "veterinarian",
                    "assignedBy" to adminUid,
                    "assignedAt" to FieldValue.serverTimestamp(),
                    "status" to "assigned",
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )

            transaction.set(
                notificationRef,
                mapOf(
                    "recipientUid" to veterinarianUid.trim(),
                    "createdBy" to adminUid,
                    "senderRole" to "administrator",
                    "title" to "Nuevo caso asignado",
                    "body" to "Se te asignó un nuevo caso para valoración y seguimiento.",
                    "type" to "case_assigned",
                    "caseId" to caseId.trim(),
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )

            veterinarianName
        }
            .addOnSuccessListener { veterinarianName ->
                callback(
                    AdminOutcome.Assigned(
                        caseId = caseId.trim(),
                        veterinarianUid = veterinarianUid.trim(),
                        veterinarianName = veterinarianName,
                    )
                )
            }
            .addOnFailureListener { error ->
                callback(AdminOutcome.Error(transactionMessageFor(error, "No fue posible asignar el caso.")))
            }
    }

    override fun reassignCase(
        caseId: String,
        veterinarianUid: String,
        callback: (AdminOutcome) -> Unit,
    ) {
        val adminUid = auth.currentUser?.uid
        if (adminUid == null) {
            callback(AdminOutcome.Error("Debes iniciar sesión como administrador para reasignar casos."))
            return
        }

        val normalizedCaseId = caseId.trim()
        val normalizedVeterinarianUid = veterinarianUid.trim()
        if (normalizedCaseId.isBlank() || normalizedVeterinarianUid.isBlank()) {
            callback(AdminOutcome.Error("Selecciona un caso y un veterinario válido."))
            return
        }

        val caseRef = firestore.collection("cases").document(normalizedCaseId)
        val veterinarianRef = firestore.collection("users").document(normalizedVeterinarianUid)
        val notificationRef = firestore.collection("notifications").document()

        firestore.runTransaction { transaction ->
            val caseSnapshot = transaction.get(caseRef)
            val veterinarianSnapshot = transaction.get(veterinarianRef)

            if (!caseSnapshot.exists()) {
                throw IllegalStateException("El caso ya no existe.")
            }

            validateVeterinarian(veterinarianSnapshot)

            if (CaseCompatibility.status(caseSnapshot) != "assigned") {
                throw IllegalStateException("Solo se puede reasignar un caso antes de iniciar la atención.")
            }

            if (caseSnapshot.getString("assignedTo").orEmpty() == normalizedVeterinarianUid) {
                throw IllegalStateException("El caso ya está asignado a ese veterinario.")
            }

            val veterinarianName = veterinarianSnapshot.getString("name").orEmpty().ifBlank { "Veterinario" }

            transaction.update(
                caseRef,
                mapOf(
                    "schemaVersion" to 2,
                    "assignedTo" to normalizedVeterinarianUid,
                    "assignedRole" to "veterinarian",
                    "assignedBy" to adminUid,
                    "assignedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )

            transaction.set(
                notificationRef,
                mapOf(
                    "recipientUid" to normalizedVeterinarianUid,
                    "createdBy" to adminUid,
                    "senderRole" to "administrator",
                    "title" to "Caso reasignado",
                    "body" to "Un caso fue reasignado a tu cuenta antes de iniciar la atención.",
                    "type" to "case_reassigned",
                    "caseId" to normalizedCaseId,
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )

            veterinarianName
        }
            .addOnSuccessListener { veterinarianName ->
                callback(
                    AdminOutcome.CaseChanged(
                        caseId = normalizedCaseId,
                        action = "reassigned",
                        veterinarianName = veterinarianName,
                    )
                )
            }
            .addOnFailureListener { error ->
                callback(AdminOutcome.Error(transactionMessageFor(error, "No fue posible reasignar el caso.")))
            }
    }

    override fun updateCasePriority(
        caseId: String,
        priority: String,
        callback: (AdminOutcome) -> Unit,
    ) {
        val adminUid = auth.currentUser?.uid
        if (adminUid == null) {
            callback(AdminOutcome.Error("Debes iniciar sesión como administrador para actualizar prioridades."))
            return
        }

        val normalizedCaseId = caseId.trim()
        val normalizedPriority = CaseCompatibility.normalizePriority(priority)
        if (normalizedCaseId.isBlank()) {
            callback(AdminOutcome.Error("El caso no es válido."))
            return
        }

        if (normalizedPriority !in setOf("low", "medium", "high", "critical")) {
            callback(AdminOutcome.Error("Selecciona una prioridad institucional válida."))
            return
        }

        val caseRef = firestore.collection("cases").document(normalizedCaseId)
        firestore.runTransaction { transaction ->
            val caseSnapshot = transaction.get(caseRef)
            if (!caseSnapshot.exists()) {
                throw IllegalStateException("El caso ya no existe.")
            }

            if (CaseCompatibility.status(caseSnapshot) == "completed") {
                throw IllegalStateException("La prioridad de un caso finalizado no puede modificarse.")
            }

            transaction.update(
                caseRef,
                mapOf(
                    "schemaVersion" to 2,
                    "finalPriority" to normalizedPriority,
                    "priorityReviewedBy" to adminUid,
                    "priorityReviewedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
        }
            .addOnSuccessListener {
                callback(
                    AdminOutcome.CaseChanged(
                        caseId = normalizedCaseId,
                        action = "priority_updated",
                    )
                )
            }
            .addOnFailureListener { error ->
                callback(AdminOutcome.Error(transactionMessageFor(error, "No fue posible actualizar la prioridad.")))
            }
    }

    private fun toAdminCaseSummary(
        document: DocumentSnapshot,
        userNames: Map<String, String>,
    ): AdminCaseSummary {
        val assignedTo = document.getString("assignedTo").orEmpty()
        return AdminCaseSummary(
            id = document.id,
            type = CaseCompatibility.text(document, "type", "caseType").ifBlank { "unknown" },
            status = CaseCompatibility.status(document),
            finalPriority = CaseCompatibility.priority(document),
            description = CaseCompatibility.text(document, "description", "details", "notes"),
            animalCount = CaseCompatibility.count(document),
            municipality = CaseCompatibility.text(document, "municipality").ifBlank { "Zipaquirá" },
            neighborhoodOrVillage = CaseCompatibility.text(
                document,
                "neighborhoodOrVillage",
                "neighborhood",
                "village",
                "sector",
            ),
            addressOrLocation = CaseCompatibility.text(
                document,
                "addressOrLocation",
                "address",
                "location",
                "property",
            ),
            referencePoint = CaseCompatibility.text(document, "referencePoint", "reference"),
            animalCondition = CaseCompatibility.text(document, "animalCondition", "condition"),
            assignedToUid = assignedTo,
            assignedToName = if (assignedTo.isBlank()) {
                "Sin asignar"
            } else {
                userNames[assignedTo] ?: "Cuenta institucional"
            },
            aiSuggestedPriority = CaseCompatibility.normalizePriority(
                document.getString("aiSuggestedPriority")
            ),
            aiPriorityReason = document.getString("aiPriorityReason").orEmpty(),
            aiModelVersion = document.getString("aiModelVersion").orEmpty(),
            legacy = CaseCompatibility.isLegacy(document),
        )
    }

    private fun validateVeterinarian(document: DocumentSnapshot) {
        val veterinarianRoles = (document.get("roles") as? List<*>)
            ?.mapNotNull { it as? String }
            .orEmpty()

        val isActiveVeterinarian = document.exists()
            && document.getString("status") == "active"
            && document.getString("activeRole") == "veterinarian"
            && veterinarianRoles.contains("veterinarian")

        if (!isActiveVeterinarian) {
            throw IllegalStateException("La cuenta seleccionada no es un veterinario activo.")
        }
    }

    private fun transactionMessageFor(error: Exception, fallback: String): String =
        if (error is IllegalStateException) {
            error.message ?: fallback
        } else {
            userMessageFor(error, fallback)
        }

    private fun userMessageFor(
        error: Exception,
        fallback: String = "No fue posible cargar el panel administrativo.",
    ): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
        else -> fallback
    }
}
