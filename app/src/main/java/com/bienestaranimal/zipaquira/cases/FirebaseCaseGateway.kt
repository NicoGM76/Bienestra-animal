package com.bienestaranimal.zipaquira.cases

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseCaseGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : CaseGateway {

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
                val cases = snapshot.documents.map { document ->
                    CaseSummary(
                        id = document.id,
                        type = document.getString("type").orEmpty(),
                        status = document.getString("status") ?: "received",
                        finalPriority = document.getString("finalPriority") ?: "pending_review",
                        neighborhoodOrVillage = document.getString("neighborhoodOrVillage").orEmpty(),
                    )
                }.sortedBy { it.type + it.neighborhoodOrVillage }
                callback(CaseOutcome.Listed(cases))
            }
            .addOnFailureListener { error -> callback(CaseOutcome.Error(userMessageFor(error))) }
    }

    private fun userMessageFor(error: Exception): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
        else -> "No fue posible completar la operación con el reporte. Intenta de nuevo."
    }
}
