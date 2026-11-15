package com.bienestaranimal.zipaquira.cases

data class PrioritySuggestion(
    val priority: String,
    val reason: String,
    val score: Int,
    val version: String = "heuristic-beta-1",
)

object CasePriorityAssistant {
    fun suggest(draft: CaseDraft): PrioritySuggestion {
        val value = draft.normalized()
        var score = 0
        val reasons = mutableListOf<String>()

        if (value.type == "abuse") {
            score += 2
            reasons += "reporte de presunto maltrato"
        }

        when (value.animalCondition.trim().lowercase()) {
            "inconsciente" -> {
                score += 5
                reasons += "animal inconsciente"
            }
            "sangrado" -> {
                score += 4
                reasons += "sangrado reportado"
            }
            "inmovilizado" -> {
                score += 4
                reasons += "animal inmovilizado"
            }
            "herido" -> {
                score += 3
                reasons += "animal herido"
            }
            "débil", "debil", "desnutrido" -> {
                score += 2
                reasons += "condición física vulnerable"
            }
            "fallecido" -> {
                score += 3
                reasons += "animal fallecido"
            }
        }

        if (value.animalCount >= 3) {
            score += 1
            reasons += "varios animales involucrados"
        }

        val description = value.description.lowercase()
        val criticalKeywords = listOf("veneno", "atropell", "arma", "sangre", "fractura")
        val welfareKeywords = listOf("sin agua", "sin comida", "abandon", "golpe", "amarrad")

        if (criticalKeywords.any(description::contains)) {
            score += 2
            reasons += "descripción con indicador de riesgo alto"
        } else if (welfareKeywords.any(description::contains)) {
            score += 1
            reasons += "descripción con indicador de bienestar comprometido"
        }

        when (CaseCompatibility.normalizePriority(value.userPriority)) {
            "critical" -> score = maxOf(score, 7)
            "high" -> score = maxOf(score, 5)
            "medium" -> score = maxOf(score, 3)
        }

        val priority = when {
            score >= 7 -> "critical"
            score >= 5 -> "high"
            score >= 3 -> "medium"
            else -> "low"
        }

        val reason = if (reasons.isEmpty()) {
            "Sin señales suficientes para elevar automáticamente la sugerencia."
        } else {
            reasons.distinct().joinToString(separator = "; ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } + "."
        }

        return PrioritySuggestion(
            priority = priority,
            reason = reason,
            score = score,
        )
    }
}
