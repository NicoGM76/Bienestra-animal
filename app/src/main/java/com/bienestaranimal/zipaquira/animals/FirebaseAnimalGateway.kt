package com.bienestaranimal.zipaquira.animals

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAnimalGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AnimalGateway {

    override fun saveAnimal(draft: AnimalDraft, callback: (AnimalOutcome) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(AnimalOutcome.Error("Debes iniciar sesión para registrar un animal."))
            return
        }

        val value = draft.normalized()
        val validationError = AnimalInputValidator.validate(value)
        if (validationError != null) {
            callback(AnimalOutcome.Error(validationError))
            return
        }

        val document = firestore.collection("animals").document()
        val data = mapOf(
            "animalId" to document.id,
            "name" to value.name,
            "species" to value.species,
            "breed" to value.breed,
            "color" to value.color,
            "sex" to value.sex,
            "approximateAge" to value.approximateAge,
            "size" to value.size,
            "sterilized" to value.sterilized,
            "addressType" to value.addressType,
            "municipality" to value.municipality,
            "neighborhoodOrVillage" to value.neighborhoodOrVillage,
            "addressOrLocation" to value.addressOrLocation,
            "referencePoint" to value.referencePoint,
            "mainResponsibleId" to user.uid,
            "createdBy" to user.uid,
            "origin" to "citizen",
            "status" to "active",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )

        document.set(data)
            .addOnSuccessListener { callback(AnimalOutcome.Saved(document.id)) }
            .addOnFailureListener { error -> callback(AnimalOutcome.Error(userMessageFor(error))) }
    }

    override fun listMyAnimals(callback: (AnimalOutcome) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(AnimalOutcome.Error("Debes iniciar sesión para consultar tus animales."))
            return
        }

        firestore.collection("animals")
            .whereEqualTo("mainResponsibleId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val animals = snapshot.documents.map { document ->
                    AnimalSummary(
                        id = document.id,
                        name = document.getString("name").orEmpty(),
                        species = document.getString("species").orEmpty(),
                        breed = document.getString("breed").orEmpty(),
                        status = document.getString("status") ?: "active",
                    )
                }.sortedBy { it.name.lowercase() }
                callback(AnimalOutcome.Listed(animals))
            }
            .addOnFailureListener { error -> callback(AnimalOutcome.Error(userMessageFor(error))) }
    }

    private fun userMessageFor(error: Exception): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión con el servicio. Revisa tu Internet."
        else -> "No fue posible completar la operación con el animal. Intenta de nuevo."
    }
}
