package com.bienestaranimal.zipaquira.cases

data class CaseSummary(
    val id: String,
    val type: String,
    val status: String,
    val finalPriority: String,
    val neighborhoodOrVillage: String,
    val addressOrLocation: String,
    val referencePoint: String,
    val animalCondition: String,
)
