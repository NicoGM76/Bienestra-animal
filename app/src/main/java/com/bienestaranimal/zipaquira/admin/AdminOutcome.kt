package com.bienestaranimal.zipaquira.admin

data class AdminUserSummary(
    val uid: String,
    val name: String,
    val email: String,
    val activeRole: String,
    val status: String,
)

data class AdminAnimalSummary(
    val id: String,
    val name: String,
    val species: String,
    val status: String,
)

data class AdminCaseSummary(
    val id: String,
    val type: String,
    val status: String,
    val finalPriority: String,
    val neighborhoodOrVillage: String,
    val animalCondition: String,
    val assignedToName: String,
)

sealed class AdminOutcome {
    data class Loaded(
        val userCount: Int,
        val animalCount: Int,
        val caseCount: Int,
        val activeCaseCount: Int,
        val citizenCount: Int,
        val veterinarianCount: Int,
        val administratorCount: Int,
        val receivedCaseCount: Int,
        val assignedCaseCount: Int,
        val inProgressCaseCount: Int,
        val completedCaseCount: Int,
        val users: List<AdminUserSummary>,
        val animals: List<AdminAnimalSummary>,
        val cases: List<AdminCaseSummary>,
    ) : AdminOutcome()

    data class Error(val message: String) : AdminOutcome()
}
