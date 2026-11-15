package com.bienestaranimal.zipaquira.animals

data class AnimalSummary(
    val id: String,
    val name: String,
    val species: String,
    val breed: String,
    val color: String,
    val sex: String,
    val approximateAge: String,
    val size: String,
    val sterilized: Boolean,
    val addressType: String,
    val municipality: String,
    val neighborhoodOrVillage: String,
    val addressOrLocation: String,
    val referencePoint: String,
    val status: String,
)

sealed class AnimalOutcome {
    data class Saved(val animalId: String) : AnimalOutcome()
    data class Updated(val animalId: String) : AnimalOutcome()
    data class Listed(val animals: List<AnimalSummary>) : AnimalOutcome()
    data class Error(val message: String) : AnimalOutcome()
}
