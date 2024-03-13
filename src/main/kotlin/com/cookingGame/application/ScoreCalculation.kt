package com.cookingGame.application

import com.cookingGame.domain.IngredientViewState
import com.cookingGame.domain.ScoreCalculationInput
import java.math.BigDecimal
import kotlin.math.absoluteValue

const val CORRECT_PREPARATION_BONUS = 100
const val INCORRECT_PREPARATION_PENALTY = 50
const val ADDITION_TIME_WINDOW = 1000 // 1 second
const val CORRECT_ADDITION_BONUS = 75
const val INCORRECT_ADDITION_PENALTY = 25

fun ScoreCalculationInput.calculateScore(): Int {
    var totalScore = 0

    for (ingredient in ingredients) {
        totalScore += calculateIngredientScore(ingredient)
    }

    return totalScore
}
fun calculateIngredientScore(ingredient: IngredientViewState): Int {
    var ingredientScore = 0

    // Score based on preparation timestamps and quantity
    val preparationCount = ingredient.preparationTimestamps.value.size
    val requiredQuantity = ingredient.quantity.value
    if (preparationCount == requiredQuantity) {
        ingredientScore += CORRECT_PREPARATION_BONUS
    } else {
        val overCount = maxOf(preparationCount - requiredQuantity, 0)
        ingredientScore -= INCORRECT_PREPARATION_PENALTY * (overCount + 1)
    }

    // Score based on input time and addition time
    if (ingredient.addedTimestamps.value.isNotEmpty()) {
        val inputTimeMillis = (ingredient.inputTime.value * BigDecimal(60) * BigDecimal(1000)).toLong() // Convert BigDecimal minutes to milliseconds
        val lastAddedTimestamp = ingredient.addedTimestamps.value.last().value.toEpochMilliseconds()
        val additionTimeDiffMillis = (inputTimeMillis - lastAddedTimestamp).absoluteValue

        if (additionTimeDiffMillis <= ADDITION_TIME_WINDOW) {
            ingredientScore += CORRECT_ADDITION_BONUS
        } else {
            ingredientScore -= INCORRECT_ADDITION_PENALTY
        }
    }

    return ingredientScore
}