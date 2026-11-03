package com.bienestaranimal.zipaquira.animals

data class AnimalDraft(
    val name: String,
    val species: String,
    val breed: String = "",
    val color: String = "",
    val sex: String = "No determinado",
    val approximateAge: String = "",
    val size: String = "Mediano",
    val sterilized: Boolean = false,
    val addressType: String = "",
    val municipality: String = "Zipaquirá",
    val neighborhoodOrVillage: String = "",
    val addressOrLocation: String = "",
    val referencePoint: String = "",
) {
    fun normalized(): AnimalDraft = copy(
        name = name.trim(),
        species = species.trim(),
        breed = breed.trim(),
        color = color.trim(),
        sex = sex.trim().ifBlank { "No determinado" },
        approximateAge = approximateAge.trim(),
        size = size.trim().ifBlank { "Mediano" },
        addressType = addressType.trim(),
        municipality = municipality.trim().ifBlank { "Zipaquirá" },
        neighborhoodOrVillage = neighborhoodOrVillage.trim(),
        addressOrLocation = addressOrLocation.trim(),
        referencePoint = referencePoint.trim(),
    )
}
