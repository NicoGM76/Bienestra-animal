package com.bienestaranimal.zipaquira.animals

data class AnimalSummary(
    val id: String,
    val name: String,
    val species: String,
    val breed: String,
    val status: String,
)

sealed class AnimalOutcome {
    data class Saved(val animalId: String) : AnimalOutcome()
    data class Listed(val animals: List<AnimalSummary>) : AnimalOutcome()
    data class Error(val message: String) : AnimalOutcome()
}
