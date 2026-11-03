package com.bienestaranimal.zipaquira.animals

object AnimalInputValidator {
    fun validate(draft: AnimalDraft): String? {
        val value = draft.normalized()
        if (value.name.isBlank()) return "El nombre del animal es obligatorio."
        if (value.species.isBlank()) return "Selecciona la especie del animal."
        return null
    }
}
