package com.bienestaranimal.zipaquira.animals

interface AnimalGateway {
    fun saveAnimal(draft: AnimalDraft, callback: (AnimalOutcome) -> Unit)
    fun listMyAnimals(callback: (AnimalOutcome) -> Unit)
}
