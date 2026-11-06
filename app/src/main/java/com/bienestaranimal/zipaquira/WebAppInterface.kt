package com.bienestaranimal.zipaquira

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.bienestaranimal.zipaquira.auth.AuthGateway
import com.bienestaranimal.zipaquira.admin.AdminGateway
import com.bienestaranimal.zipaquira.admin.AdminOutcome
import com.bienestaranimal.zipaquira.auth.AuthInputValidator
import com.bienestaranimal.zipaquira.auth.AuthOutcome
import com.bienestaranimal.zipaquira.animals.AnimalDraft
import com.bienestaranimal.zipaquira.animals.AnimalGateway
import com.bienestaranimal.zipaquira.animals.AnimalInputValidator
import com.bienestaranimal.zipaquira.animals.AnimalOutcome
import com.bienestaranimal.zipaquira.cases.CaseDraft
import com.bienestaranimal.zipaquira.cases.CaseGateway
import com.bienestaranimal.zipaquira.cases.CaseInputValidator
import com.bienestaranimal.zipaquira.cases.CaseOutcome
import com.bienestaranimal.zipaquira.web.BridgeMessagePolicy
import com.bienestaranimal.zipaquira.web.WebEventSink
import org.json.JSONArray
import org.json.JSONObject

/**
 * Puente mínimo entre el frontend HTML/JS y Android.
 * El frontend solicita acciones; Kotlin valida y delega al backend.
 */
class WebAppInterface(
    private val context: Context,
    private val authGateway: AuthGateway,
    private val animalGateway: AnimalGateway,
    private val caseGateway: CaseGateway,
    private val adminGateway: AdminGateway,
    private val eventSink: WebEventSink,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun login(email: String, password: String) {
        val validationError = AuthInputValidator.validateLogin(email, password)
        if (validationError != null) {
            emitError(validationError)
            return
        }

        eventSink.emit("auth:loading", JSONObject().put("action", "login").toString())
        authGateway.signIn(email, password) { outcome ->
            when (outcome) {
                is AuthOutcome.Success -> eventSink.emit(
                    "auth:success",
                    JSONObject()
                        .put("uid", outcome.uid)
                        .put("email", outcome.email)
                        .put("name", outcome.name)
                        .put("activeRole", outcome.activeRole)
                        .put("roles", JSONArray(outcome.roles))
                        .toString(),
                )
                is AuthOutcome.Error -> emitError(outcome.message)
            }
        }
    }

    @JavascriptInterface
    fun registerCitizen(name: String, email: String, password: String) {
        val validationError = AuthInputValidator.validateRegistration(name, email, password)
        if (validationError != null) {
            emitError(validationError)
            return
        }

        eventSink.emit("auth:loading", JSONObject().put("action", "register").toString())
        authGateway.registerCitizen(name, email, password) { outcome ->
            when (outcome) {
                is AuthOutcome.Success -> eventSink.emit(
                    "auth:registered",
                    JSONObject()
                        .put("uid", outcome.uid)
                        .put("email", outcome.email)
                        .put("name", outcome.name)
                        .put("activeRole", outcome.activeRole)
                        .put("roles", JSONArray(outcome.roles))
                        .toString(),
                )
                is AuthOutcome.Error -> emitError(outcome.message)
            }
        }
    }


    @JavascriptInterface
    fun logout() {
        authGateway.signOut()
        eventSink.emit("auth:signedout", "{}")
    }


    @JavascriptInterface
    fun saveAnimal(payloadJson: String) {
        val draft = try {
            val payload = JSONObject(payloadJson)
            AnimalDraft(
                name = payload.optString("name"),
                species = payload.optString("species"),
                breed = payload.optString("breed"),
                color = payload.optString("color"),
                sex = payload.optString("sex", "No determinado"),
                approximateAge = payload.optString("approximateAge"),
                size = payload.optString("size", "Mediano"),
                sterilized = payload.optBoolean("sterilized", false),
                addressType = payload.optString("addressType"),
                municipality = payload.optString("municipality", "Zipaquirá"),
                neighborhoodOrVillage = payload.optString("neighborhoodOrVillage"),
                addressOrLocation = payload.optString("addressOrLocation"),
                referencePoint = payload.optString("referencePoint"),
            )
        } catch (_: Exception) {
            emitAnimalError("No fue posible leer los datos del animal.")
            return
        }

        val validationError = AnimalInputValidator.validate(draft)
        if (validationError != null) {
            emitAnimalError(validationError)
            return
        }

        eventSink.emit("animals:loading", JSONObject().put("action", "save").toString())
        animalGateway.saveAnimal(draft) { outcome ->
            when (outcome) {
                is AnimalOutcome.Saved -> eventSink.emit(
                    "animals:saved",
                    JSONObject().put("animalId", outcome.animalId).toString(),
                )
                is AnimalOutcome.Error -> emitAnimalError(outcome.message)
                is AnimalOutcome.Listed -> emitAnimalList(outcome)
            }
        }
    }

    @JavascriptInterface
    fun loadMyAnimals() {
        eventSink.emit("animals:loading", JSONObject().put("action", "list").toString())
        animalGateway.listMyAnimals { outcome ->
            when (outcome) {
                is AnimalOutcome.Listed -> emitAnimalList(outcome)
                is AnimalOutcome.Error -> emitAnimalError(outcome.message)
                is AnimalOutcome.Saved -> Unit
            }
        }
    }


    @JavascriptInterface
    fun saveCase(payloadJson: String) {
        val draft = try {
            val payload = JSONObject(payloadJson)
            CaseDraft(
                type = payload.optString("type"),
                description = payload.optString("description"),
                userPriority = payload.optString("userPriority", "system"),
                animalCount = payload.optInt("animalCount", 1),
                animalCondition = payload.optString("animalCondition"),
                municipality = payload.optString("municipality", "Zipaquirá"),
                neighborhoodOrVillage = payload.optString("neighborhoodOrVillage"),
                addressOrLocation = payload.optString("addressOrLocation"),
                referencePoint = payload.optString("referencePoint"),
                responsibleKnown = payload.optString("responsibleKnown", "unknown"),
                suspectedResponsible = payload.optString("suspectedResponsible"),
            )
        } catch (_: Exception) {
            emitCaseError("No fue posible leer los datos del reporte.")
            return
        }

        val validationError = CaseInputValidator.validate(draft)
        if (validationError != null) {
            emitCaseError(validationError)
            return
        }

        eventSink.emit("cases:loading", JSONObject().put("action", "save").toString())
        caseGateway.saveCase(draft) { outcome ->
            when (outcome) {
                is CaseOutcome.Saved -> eventSink.emit(
                    "cases:saved",
                    JSONObject().put("caseId", outcome.caseId).toString(),
                )
                is CaseOutcome.Error -> emitCaseError(outcome.message)
                is CaseOutcome.Listed -> emitCaseList(outcome)
                is CaseOutcome.Updated -> Unit
                is CaseOutcome.CareSaved -> Unit
            }
        }
    }

    @JavascriptInterface
    fun loadMyCases() {
        eventSink.emit("cases:loading", JSONObject().put("action", "list").toString())
        caseGateway.listMyCases { outcome ->
            when (outcome) {
                is CaseOutcome.Listed -> emitCaseList(outcome)
                is CaseOutcome.Error -> emitCaseError(outcome.message)
                is CaseOutcome.Saved -> Unit
                is CaseOutcome.Updated -> Unit
                is CaseOutcome.CareSaved -> Unit
            }
        }
    }

    @JavascriptInterface
    fun loadAssignedCases() {
        eventSink.emit("vet:loading", JSONObject().put("action", "list").toString())
        caseGateway.listAssignedCases { outcome ->
            when (outcome) {
                is CaseOutcome.Listed -> emitVeterinarianCaseList(outcome)
                is CaseOutcome.Error -> emitVeterinarianError(outcome.message)
                is CaseOutcome.Saved -> Unit
                is CaseOutcome.Updated -> Unit
                is CaseOutcome.CareSaved -> Unit
            }
        }
    }

    @JavascriptInterface
    fun updateAssignedCaseStatus(caseId: String, newStatus: String) {
        eventSink.emit("vet:loading", JSONObject().put("action", "update").toString())
        caseGateway.updateAssignedCaseStatus(caseId, newStatus) { outcome ->
            when (outcome) {
                is CaseOutcome.Updated -> eventSink.emit(
                    "vet:case-updated",
                    JSONObject()
                        .put("caseId", outcome.caseId)
                        .put("status", outcome.status)
                        .toString(),
                )
                is CaseOutcome.Error -> emitVeterinarianError(outcome.message)
                is CaseOutcome.Saved -> Unit
                is CaseOutcome.Listed -> Unit
                is CaseOutcome.CareSaved -> Unit
            }
        }
    }

    @JavascriptInterface
    fun saveVeterinaryCare(
        caseId: String,
        diagnosis: String,
        observations: String,
        treatment: String,
        recommendations: String,
    ) {
        eventSink.emit("vet:loading", JSONObject().put("action", "care").toString())
        caseGateway.saveVeterinaryCare(
            caseId = caseId,
            diagnosis = diagnosis,
            observations = observations,
            treatment = treatment,
            recommendations = recommendations,
        ) { outcome ->
            when (outcome) {
                is CaseOutcome.CareSaved -> eventSink.emit(
                    "vet:care-saved",
                    JSONObject().put("caseId", outcome.caseId).toString(),
                )
                is CaseOutcome.Error -> emitVeterinarianError(outcome.message)
                is CaseOutcome.Saved -> Unit
                is CaseOutcome.Updated -> Unit
                is CaseOutcome.Listed -> Unit
            }
        }
    }

    @JavascriptInterface
    fun loadAdminDashboard() {
        eventSink.emit("admin:loading", "{}")
        adminGateway.loadDashboard { outcome ->
            when (outcome) {
                is AdminOutcome.Loaded -> emitAdminDashboard(outcome)
                is AdminOutcome.Error -> eventSink.emit(
                    "admin:error",
                    JSONObject().put("message", outcome.message).toString(),
                )
            }
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        val safeMessage = BridgeMessagePolicy.sanitize(message) ?: return
        mainHandler.post {
            Toast.makeText(context.applicationContext, safeMessage, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun getPlatform(): String = "Android / Kotlin"

    @JavascriptInterface
    fun getEnvironment(): String = BuildConfig.ENVIRONMENT


    private fun emitAnimalList(outcome: AnimalOutcome.Listed) {
        val animals = JSONArray()
        outcome.animals.forEach { animal ->
            animals.put(
                JSONObject()
                    .put("id", animal.id)
                    .put("name", animal.name)
                    .put("species", animal.species)
                    .put("breed", animal.breed)
                    .put("status", animal.status),
            )
        }
        eventSink.emit("animals:list", JSONObject().put("animals", animals).toString())
    }


    private fun emitCaseList(outcome: CaseOutcome.Listed) {
        val cases = JSONArray()
        outcome.cases.forEach { item ->
            cases.put(
                JSONObject()
                    .put("id", item.id)
                    .put("type", item.type)
                    .put("status", item.status)
                    .put("finalPriority", item.finalPriority)
                    .put("neighborhoodOrVillage", item.neighborhoodOrVillage)
                    .put("animalCondition", item.animalCondition)
                    .put("assignedToName", item.assignedToName),
            )
        }
        eventSink.emit("cases:list", JSONObject().put("cases", cases).toString())
    }

    private fun emitAdminDashboard(outcome: AdminOutcome.Loaded) {
        val users = JSONArray()
        outcome.users.forEach { user ->
            users.put(
                JSONObject()
                    .put("uid", user.uid)
                    .put("name", user.name)
                    .put("email", user.email)
                    .put("activeRole", user.activeRole)
                    .put("status", user.status),
            )
        }

        val animals = JSONArray()
        outcome.animals.forEach { animal ->
            animals.put(
                JSONObject()
                    .put("id", animal.id)
                    .put("name", animal.name)
                    .put("species", animal.species)
                    .put("status", animal.status),
            )
        }

        val cases = JSONArray()
        outcome.cases.forEach { item ->
            cases.put(
                JSONObject()
                    .put("id", item.id)
                    .put("type", item.type)
                    .put("status", item.status)
                    .put("finalPriority", item.finalPriority)
                    .put("neighborhoodOrVillage", item.neighborhoodOrVillage),
            )
        }

        eventSink.emit(
            "admin:dashboard",
            JSONObject()
                .put("userCount", outcome.userCount)
                .put("animalCount", outcome.animalCount)
                .put("caseCount", outcome.caseCount)
                .put("activeCaseCount", outcome.activeCaseCount)
                .put("citizenCount", outcome.citizenCount)
                .put("veterinarianCount", outcome.veterinarianCount)
                .put("administratorCount", outcome.administratorCount)
                .put("receivedCaseCount", outcome.receivedCaseCount)
                .put("assignedCaseCount", outcome.assignedCaseCount)
                .put("inProgressCaseCount", outcome.inProgressCaseCount)
                .put("completedCaseCount", outcome.completedCaseCount)
                .put("users", users)
                .put("animals", animals)
                .put("cases", cases)
                .toString(),
        )
    }

    private fun emitVeterinarianCaseList(outcome: CaseOutcome.Listed) {
        val cases = JSONArray()
        outcome.cases.forEach { item ->
            cases.put(
                JSONObject()
                    .put("id", item.id)
                    .put("type", item.type)
                    .put("status", item.status)
                    .put("finalPriority", item.finalPriority)
                    .put("description", item.description)
                    .put("animalCount", item.animalCount)
                    .put("neighborhoodOrVillage", item.neighborhoodOrVillage)
                    .put("addressOrLocation", item.addressOrLocation)
                    .put("referencePoint", item.referencePoint)
                    .put("animalCondition", item.animalCondition)
                    .put("veterinaryDiagnosis", item.veterinaryDiagnosis)
                    .put("veterinaryObservations", item.veterinaryObservations)
                    .put("veterinaryTreatment", item.veterinaryTreatment)
                    .put("veterinaryRecommendations", item.veterinaryRecommendations),
            )
        }
        eventSink.emit("vet:cases", JSONObject().put("cases", cases).toString())
    }

    private fun emitVeterinarianError(message: String) {
        eventSink.emit("vet:error", JSONObject().put("message", message).toString())
    }

    private fun emitCaseError(message: String) {
        eventSink.emit("cases:error", JSONObject().put("message", message).toString())
    }

    private fun emitAnimalError(message: String) {
        eventSink.emit("animals:error", JSONObject().put("message", message).toString())
    }

    private fun emitError(message: String) {
        eventSink.emit("auth:error", JSONObject().put("message", message).toString())
    }
}
