package com.bienestaranimal.zipaquira.notifications

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseNotificationGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : NotificationGateway {

    override fun listMine(callback: (NotificationOutcome) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(NotificationOutcome.Error("Debes iniciar sesión para consultar notificaciones."))
            return
        }

        firestore.collection("notifications")
            .whereEqualTo("recipientUid", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val notifications = snapshot.documents
                    .map { document ->
                        NotificationSummary(
                            id = document.id,
                            title = document.getString("title").orEmpty(),
                            body = document.getString("body").orEmpty(),
                            type = document.getString("type").orEmpty(),
                            caseId = document.getString("caseId").orEmpty(),
                            read = document.getBoolean("read") ?: false,
                            createdAtMillis = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        )
                    }
                    .sortedByDescending { it.createdAtMillis }
                    .take(30)

                callback(NotificationOutcome.Listed(notifications))
            }
            .addOnFailureListener { error ->
                callback(NotificationOutcome.Error(userMessageFor(error)))
            }
    }

    override fun markRead(notificationId: String, callback: (NotificationOutcome) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(NotificationOutcome.Error("Debes iniciar sesión para actualizar notificaciones."))
            return
        }

        val normalizedId = notificationId.trim()
        if (normalizedId.isBlank()) {
            callback(NotificationOutcome.Error("La notificación no es válida."))
            return
        }

        val ref = firestore.collection("notifications").document(normalizedId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            if (!snapshot.exists()) throw IllegalStateException("La notificación ya no existe.")
            if (snapshot.getString("recipientUid") != uid) {
                throw IllegalStateException("No puedes modificar esta notificación.")
            }

            transaction.update(
                ref,
                mapOf(
                    "read" to true,
                    "readAt" to FieldValue.serverTimestamp(),
                ),
            )
        }
            .addOnSuccessListener { callback(NotificationOutcome.Read(normalizedId)) }
            .addOnFailureListener { error ->
                callback(
                    NotificationOutcome.Error(
                        if (error is IllegalStateException) {
                            error.message ?: "No fue posible actualizar la notificación."
                        } else {
                            userMessageFor(error)
                        }
                    )
                )
            }
    }

    private fun userMessageFor(error: Exception): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
        else -> "No fue posible cargar las notificaciones."
    }
}
