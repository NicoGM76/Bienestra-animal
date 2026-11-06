package com.bienestaranimal.zipaquira.cases

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseCaseGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : CaseGateway {

    private val allowedTransitions = setOf(
        "assigned" to "in_progress",
        "in_progress" to "completed",
    )

    override fun saveCase(draft: CaseDraft, callback: (CaseOutcome) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(CaseOutcome.Error("Debes iniciar sesión para enviar un reporte."))
            return
        }

        val value = draft.normalized()
        val validationError = CaseInputValidator.validate(value)
        if (validationError != null) {
            callback(CaseOutcome.Error(validationError))
            return
        }

        val document = firestore.collection("cases").document()
        val isProtected = value.type == "abuse"
        val finalPriority = if (value.userPriority == "system") "pending_review" else value.userPriority
        val data = mapOf(
            "caseId" to document.id,
            "type" to value.type,
            "description" to value.description,
            "animalCount" to value.animalCount,
            "animalCondition" to value.animalCondition,
            "userPriority" to value.userPriority,
            "aiSuggestedPriority" to null,
            "finalPriority" to finalPriority,
            "status" to "received",
            "municipality" to value.municipality,
            "neighborhoodOrVillage" to value.neighborhoodOrVillage,
            "addressOrLocation" to value.addressOrLocation,
            "referencePoint" to value.referencePoint,
            "responsibleKnown" to value.responsibleKnown,
            "suspectedResponsible" to value.suspectedResponsible,
            "protectedIdentity" to isProtected,
            "createdBy" to user.uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )

        document.set(data)
            .addOnSuccessListener { callback(CaseOutcome.Saved(document.id)) }
            .addOnFailureListener { error -> callback(CaseOutcome.Error(userMessageFor(error))) }
    }

    override fun listMyCases(callback: (CaseOutcome) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(CaseOutcome.Error("Debes iniciar sesión para consultar tus reportes."))
            return
        }

        firestore.collection("cases")
            .whereEqualTo("createdBy", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                callback(
                    CaseOutcome.Listed(
                        snapshot.documents
                            .map(::toSummary)
                            .sortedBy { it.type + it.neighborhoodOrVillage }
                    )
                )
            }
            .addOnFailureListener { error -> callback(CaseOutcome.Error(userMessageFor(error))) }
    }

    override fun listAssignedCases(callback: (CaseOutcome) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(CaseOutcome.Error("Debes iniciar sesión para consultar casos asignados."))
            return
        }

        firestore.collection("cases")
            .whereEqualTo("assignedTo", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                callback(
                    CaseOutcome.Listed(
                        snapshot.documents
                            .map(::toSummary)
                            .sortedWith(compareBy<CaseSummary> { it.status }.thenBy { it.finalPriority })
                    )
                )
            }
            .addOnFailureListener { error -> callback(CaseOutcome.Error(userMessageFor(error))) }
    }

    override fun updateAssignedCaseStatus(
        caseId: String,
        newStatus: String,
        callback: (CaseOutcome) -> Unit,
    ) {
        val user = auth.currentUser
        if (user == null) {
            callback(CaseOutcome.Error("Debes iniciar sesión para actualizar un caso."))
            return
        }

        val normalizedId = caseId.trim()
        val normalizedStatus = newStatus.trim()
        if (normalizedId.isBlank()) {
            callback(CaseOutcome.Error("El caso no es válido."))
            return
        }

        val document = firestore.collection("cases").document(normalizedId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            if (!snapshot.exists()) {
                throw IllegalStateException("El caso no existe.")
            }

            val assignedTo = snapshot.getString("assignedTo").orEmpty()
            if (assignedTo != user.uid) {
                throw IllegalStateException("Este caso no está asignado a tu cuenta.")
            }

            val currentStatus = snapshot.getString("status").orEmpty()
            if ((currentStatus to normalizedStatus) !in allowedTransitions) {
                throw IllegalStateException("La transición de estado no está permitida.")
            }

            transaction.update(
                document,
                mapOf(
                    "status" to normalizedStatus,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
        }.addOnSuccessListener {
            callback(CaseOutcome.Updated(normalizedId, normalizedStatus))
        }.addOnFailureListener { error ->
            callback(CaseOutcome.Error(transactionMessageFor(error, "No fue posible actualizar el caso.")))
        }
    }

    override fun saveVeterinaryCare(
        caseId: String,
        diagnosis: String,
        observations: String,
        treatment: String,
        recommendations: String,
        callback: (CaseOutcome) -> Unit,
    ) {
        val user = auth.currentUser
        if (user == null) {
            callback(CaseOutcome.Error("Debes iniciar sesión para registrar la atención."))
            return
        }

        val normalizedId = caseId.trim()
        val normalizedDiagnosis = diagnosis.trim()
        val normalizedObservations = observations.trim()
        val normalizedTreatment = treatment.trim()
        val normalizedRecommendations = recommendations.trim()

        if (normalizedId.isBlank()) {
            callback(CaseOutcome.Error("El caso no es válido."))
            return
        }

        if (normalizedDiagnosis.length < 3) {
            callback(CaseOutcome.Error("Registra un diagnóstico o impresión clínica."))
            return
        }

        if (normalizedTreatment.length < 3) {
            callback(CaseOutcome.Error("Registra el tratamiento o conducta indicada."))
            return
        }

        val document = firestore.collection("cases").document(normalizedId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            if (!snapshot.exists()) {
                throw IllegalStateException("El caso no existe.")
            }

            if (snapshot.getString("assignedTo").orEmpty() != user.uid) {
                throw IllegalStateException("Este caso no está asignado a tu cuenta.")
            }

            if (snapshot.getString("status").orEmpty() != "in_progress") {
                throw IllegalStateException("Inicia la atención antes de registrar la ficha clínica.")
            }

            transaction.update(
                document,
                mapOf(
                    "veterinaryDiagnosis" to normalizedDiagnosis,
                    "veterinaryObservations" to normalizedObservations,
                    "veterinaryTreatment" to normalizedTreatment,
                    "veterinaryRecommendations" to normalizedRecommendations,
                    "veterinaryUpdatedBy" to user.uid,
                    "veterinaryUpdatedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
        }.addOnSuccessListener {
            callback(CaseOutcome.CareSaved(normalizedId))
        }.addOnFailureListener { error ->
            callback(CaseOutcome.Error(transactionMessageFor(error, "No fue posible guardar la atención veterinaria.")))
        }
    }

    private fun toSummary(document: com.google.firebase.firestore.DocumentSnapshot): CaseSummary =
        CaseSummary(
            id = document.id,
            type = document.getString("type").orEmpty(),
            status = document.getString("status") ?: "received",
            finalPriority = document.getString("finalPriority") ?: "pending_review",
            description = document.getString("description").orEmpty(),
            animalCount = (document.getLong("animalCount") ?: 1L).toInt(),
            neighborhoodOrVillage = document.getString("neighborhoodOrVillage").orEmpty(),
            addressOrLocation = document.getString("addressOrLocation").orEmpty(),
            referencePoint = document.getString("referencePoint").orEmpty(),
            animalCondition = document.getString("animalCondition").orEmpty(),
            veterinaryDiagnosis = document.getString("veterinaryDiagnosis").orEmpty(),
            veterinaryObservations = document.getString("veterinaryObservations").orEmpty(),
            veterinaryTreatment = document.getString("veterinaryTreatment").orEmpty(),
            veterinaryRecommendations = document.getString("veterinaryRecommendations").orEmpty(),
        )

    private fun transactionMessageFor(error: Exception, fallback: String): String =
        if (error is IllegalStateException) {
            error.message ?: fallback
        } else {
            userMessageFor(error, fallback)
        }

    private fun userMessageFor(error: Exception, fallback: String = "No fue posible completar la operación con el reporte. Intenta de nuevo."): String =
        when (error) {
            is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
            else -> fallback
        }
}
