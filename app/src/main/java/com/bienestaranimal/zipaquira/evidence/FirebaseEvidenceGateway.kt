package com.bienestaranimal.zipaquira.evidence

import android.net.Uri
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FirebaseEvidenceGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) : EvidenceGateway {

    override fun uploadCaseImage(caseId: String, uri: Uri, callback: (EvidenceOutcome) -> Unit) {
        val user = auth.currentUser
        val normalizedCaseId = caseId.trim()
        if (user == null) {
            callback(EvidenceOutcome.Error("Debes iniciar sesión para adjuntar evidencia."))
            return
        }
        if (normalizedCaseId.isBlank()) {
            callback(EvidenceOutcome.Error("El caso no es válido."))
            return
        }

        val caseRef = firestore.collection("cases").document(normalizedCaseId)
        caseRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists() || snapshot.getString("createdBy") != user.uid) {
                    callback(EvidenceOutcome.Error("Solo el ciudadano que creó el reporte puede adjuntar evidencia."))
                    return@addOnSuccessListener
                }

                val evidenceId = UUID.randomUUID().toString()
                val storageRef = storage.reference.child("caseEvidence/$normalizedCaseId/$evidenceId")
                storageRef.putFile(uri)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            throw task.exception ?: IllegalStateException("No fue posible subir la evidencia.")
                        }
                        storageRef.downloadUrl
                    }
                    .addOnSuccessListener { downloadUri ->
                        caseRef.update(
                            mapOf(
                                "evidenceUrls" to FieldValue.arrayUnion(downloadUri.toString()),
                                "evidenceStoragePaths" to FieldValue.arrayUnion(storageRef.path),
                                "evidenceUpdatedAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp(),
                            )
                        )
                            .addOnSuccessListener {
                                callback(
                                    EvidenceOutcome.Uploaded(
                                        caseId = normalizedCaseId,
                                        storagePath = storageRef.path,
                                        downloadUrl = downloadUri.toString(),
                                    )
                                )
                            }
                            .addOnFailureListener { error ->
                                callback(EvidenceOutcome.Error(userMessageFor(error)))
                            }
                    }
                    .addOnFailureListener { error ->
                        callback(EvidenceOutcome.Error(userMessageFor(error)))
                    }
            }
            .addOnFailureListener { error -> callback(EvidenceOutcome.Error(userMessageFor(error))) }
    }

    private fun userMessageFor(error: Exception): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión para subir la fotografía. Intenta cuando recuperes Internet."
        else -> "No fue posible adjuntar la fotografía al reporte."
    }
}
