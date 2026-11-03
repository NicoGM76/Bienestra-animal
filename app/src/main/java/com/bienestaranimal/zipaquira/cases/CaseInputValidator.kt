package com.bienestaranimal.zipaquira.cases

object CaseInputValidator {
    private val allowedTypes = setOf("lost", "found", "abuse")
    private val allowedPriorities = setOf("system", "low", "medium", "high", "critical")

    fun validate(draft: CaseDraft): String? {
        val value = draft.normalized()
        if (value.type !in allowedTypes) return "Selecciona un tipo de reporte válido."
        if (value.description.length < 10) return "Describe lo ocurrido con un poco más de detalle."
        if (value.userPriority !in allowedPriorities) return "Selecciona una prioridad válida."
        if (value.animalCondition.isBlank()) return "Indica el estado del animal."
        if (value.municipality.lowercase() != "zipaquirá") return "La demo está configurada para Zipaquirá."
        if (value.neighborhoodOrVillage.isBlank()) return "Indica el barrio, vereda o sector."
        if (value.addressOrLocation.isBlank()) return "Indica la dirección o ubicación del caso."
        if (value.referencePoint.isBlank()) return "Agrega un punto de referencia."
        return null
    }
}
