package com.bienestaranimal.zipaquira.cases

data class CaseDraft(
    val type: String,
    val description: String,
    val userPriority: String,
    val animalCount: Int,
    val animalCondition: String,
    val municipality: String,
    val neighborhoodOrVillage: String,
    val addressOrLocation: String,
    val referencePoint: String,
    val responsibleKnown: String,
    val suspectedResponsible: String,
) {
    fun normalized(): CaseDraft = copy(
        type = type.trim().lowercase(),
        description = description.trim(),
        userPriority = userPriority.trim().lowercase(),
        animalCount = animalCount.coerceAtLeast(1),
        animalCondition = animalCondition.trim(),
        municipality = municipality.trim().ifBlank { "Zipaquirá" },
        neighborhoodOrVillage = neighborhoodOrVillage.trim(),
        addressOrLocation = addressOrLocation.trim(),
        referencePoint = referencePoint.trim(),
        responsibleKnown = responsibleKnown.trim().lowercase(),
        suspectedResponsible = suspectedResponsible.trim(),
    )
}
