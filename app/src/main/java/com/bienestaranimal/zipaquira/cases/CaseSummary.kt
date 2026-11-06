package com.bienestaranimal.zipaquira.cases

data class CaseSummary(
    val id: String,
    val type: String,
    val status: String,
    val finalPriority: String,
    val description: String,
    val animalCount: Int,
    val neighborhoodOrVillage: String,
    val addressOrLocation: String,
    val referencePoint: String,
    val animalCondition: String,
    val veterinaryDiagnosis: String,
    val veterinaryObservations: String,
    val veterinaryTreatment: String,
    val veterinaryRecommendations: String,
)
