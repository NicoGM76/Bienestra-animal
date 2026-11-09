package com.bienestaranimal.zipaquira.cases

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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

        val suggestion = CasePriorityAssistant.suggest(value)
        val document = firestore.collection("cases").document()
        val isProtected = value.type == "abuse"
        val finalPriority = if (value.userPriority == "system") "pending_review" else value.userPriority
        val data = mapOf(
            "schemaVersion" to 2,
            "caseId" to document.id,
            "type" to value.type,
            "description" to value.description,
            "animalCount" to value.animalCount,
            "animalCondition" to value.animalCondition,
            "userPriority" to value.userPriority,
            "aiSuggestedPriority" to suggestion.priority,
            "aiPriorityReason" to suggestion.reason,
            "aiPriorityScore" to suggestion.score,
            "aiModelVersion" to suggestion.version,
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
        val normalizedStatus = CaseCompatibility.normalizeStatus(newStatus)
        if (normalizedId.isBlank()) {
            callback(CaseOutcome.Error("El caso no es válido."))
            return
        }

        val document = firestore.collection("cases").document(normalizedId)
        val notificationRef = firestore.collection("notifications").document()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            if (!snapshot.exists()) {
                throw IllegalStateException("El caso no existe.")
            }

            val assignedTo = snapshot.getString("assignedTo").orEmpty()
            if (assignedTo != user.uid) {
                throw IllegalStateException("Este caso no está asignado a tu cuenta.")
            }

            val currentStatus = CaseCompatibility.status(snapshot)
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

            val citizenUid = snapshot.getString("createdBy").orEmpty()
            if (citizenUid.isNotBlank()) {
                val notification = when (normalizedStatus) {
                    "in_progress" -> mapOf(
                        "recipientUid" to citizenUid,
                        "createdBy" to user.uid,
                        "senderRole" to "veterinarian",
                        "title" to "Tu caso está en atención",
                        "body" to "El profesional asignado inició la atención de tu reporte.",
                        "type" to "case_in_progress",
                        "caseId" to normalizedId,
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                    )
                    "completed" -> mapOf(
                        "recipientUid" to citizenUid,
                        "createdBy" to user.uid,
                        "senderRole" to "veterinarian",
                        "title" to "Atención finalizada",
                        "body" to "El caso reportado fue marcado como atendido por el profesional asignado.",
                        "type" to "case_completed",
                        "caseId" to normalizedId,
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                    )
                    else -> null
                }

                if (notification != null) {
                    transaction.set(notificationRef, notification)
                }
            }
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
        val recordRef = document.collection("attentions").document()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            if (!snapshot.exists()) {
                throw IllegalStateException("El caso no existe.")
            }

            if (snapshot.getString("assignedTo").orEmpty() != user.uid) {
                throw IllegalStateException("Este caso no está asignado a tu cuenta.")
            }

            if (CaseCompatibility.status(snapshot) != "in_progress") {
                throw IllegalStateException("Inicia la atención antes de registrar la ficha clínica.")
            }

            transaction.set(
                recordRef,
                mapOf(
                    "recordId" to recordRef.id,
                    "caseId" to normalizedId,
                    "veterinarianUid" to user.uid,
                    "diagnosis" to normalizedDiagnosis,
                    "observations" to normalizedObservations,
                    "treatment" to normalizedTreatment,
                    "recommendations" to normalizedRecommendations,
                    "source" to "veterinary_care",
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )

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
            callback(CaseOutcome.CareSaved(normalizedId, recordRef.id))
        }.addOnFailureListener { error ->
            callback(CaseOutcome.Error(transactionMessageFor(error, "No fue posible guardar la atención veterinaria.")))
        }
    }

    override fun listClinicalHistory(caseId: String, callback: (CaseOutcome) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(CaseOutcome.Error("Debes iniciar sesión para consultar el historial clínico."))
            return
        }

        val normalizedId = caseId.trim()
        if (normalizedId.isBlank()) {
            callback(CaseOutcome.Error("El caso no es válido."))
            return
        }

        val caseRef = firestore.collection("cases").document(normalizedId)
        caseRef.collection("attentions")
            .get()
            .addOnSuccessListener { snapshot ->
                val records = snapshot.documents
                    .map(::toClinicalRecord)
                    .sortedByDescending { it.createdAtMillis }

                if (records.isNotEmpty()) {
                    callback(CaseOutcome.HistoryListed(normalizedId, records))
                    return@addOnSuccessListener
                }

                caseRef.get()
                    .addOnSuccessListener { caseDocument ->
                        val diagnosis = caseDocument.getString("veterinaryDiagnosis").orEmpty()
                        val treatment = caseDocument.getString("veterinaryTreatment").orEmpty()
                        if (diagnosis.isBlank() && treatment.isBlank()) {
                            callback(CaseOutcome.HistoryListed(normalizedId, emptyList()))
                            return@addOnSuccessListener
                        }

                        val legacyRecord = ClinicalRecordSummary(
                            id = "legacy-$normalizedId",
                            diagnosis = diagnosis,
                            observations = caseDocument.getString("veterinaryObservations").orEmpty(),
                            treatment = treatment,
                            recommendations = caseDocument.getString("veterinaryRecommendations").orEmpty(),
                            veterinarianUid = caseDocument.getString("veterinaryUpdatedBy").orEmpty(),
                            createdAtMillis = (
                                caseDocument.getTimestamp("veterinaryUpdatedAt")
                                    ?: caseDocument.getTimestamp("updatedAt")
                                )?.toDate()?.time ?: 0L,
                            source = "legacy_case_snapshot",
                        )
                        callback(CaseOutcome.HistoryListed(normalizedId, listOf(legacyRecord)))
                    }
                    .addOnFailureListener { error ->
                        callback(CaseOutcome.Error(userMessageFor(error, "No fue posible recuperar el historial clínico.")))
                    }
            }
            .addOnFailureListener { error ->
                callback(CaseOutcome.Error(userMessageFor(error, "No fue posible recuperar el historial clínico.")))
            }
    }

    private fun toSummary(document: DocumentSnapshot): CaseSummary =
        CaseSummary(
            id = document.id,
            type = CaseCompatibility.text(document, "type", "caseType").ifBlank { "unknown" },
            status = CaseCompatibility.status(document),
            finalPriority = CaseCompatibility.priority(document),
            description = CaseCompatibility.text(document, "description", "details", "notes"),
            animalCount = CaseCompatibility.count(document),
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
            aiSuggestedPriority = CaseCompatibility.normalizePriority(
                document.getString("aiSuggestedPriority")
            ),
            aiPriorityReason = document.getString("aiPriorityReason").orEmpty(),
            aiModelVersion = document.getString("aiModelVersion").orEmpty(),
            legacy = CaseCompatibility.isLegacy(document),
            veterinaryDiagnosis = document.getString("veterinaryDiagnosis").orEmpty(),
            veterinaryObservations = document.getString("veterinaryObservations").orEmpty(),
            veterinaryTreatment = document.getString("veterinaryTreatment").orEmpty(),
            veterinaryRecommendations = document.getString("veterinaryRecommendations").orEmpty(),
        )

    private fun toClinicalRecord(document: DocumentSnapshot): ClinicalRecordSummary =
        ClinicalRecordSummary(
            id = document.id,
            diagnosis = document.getString("diagnosis").orEmpty(),
            observations = document.getString("observations").orEmpty(),
            treatment = document.getString("treatment").orEmpty(),
            recommendations = document.getString("recommendations").orEmpty(),
            veterinarianUid = document.getString("veterinarianUid").orEmpty(),
            createdAtMillis = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
            source = document.getString("source").orEmpty().ifBlank { "veterinary_care" },
        )

    private fun transactionMessageFor(error: Exception, fallback: String): String =
        if (error is IllegalStateException) {
            error.message ?: fallback
        } else {
            userMessageFor(error, fallback)
        }

    private fun userMessageFor(
        error: Exception,
        fallback: String = "No fue posible completar la operación con el reporte. Intenta de nuevo.",
    ): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
        else -> fallback
    }
}
