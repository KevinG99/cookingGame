package com.cookingGame.domain

import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.flow.emptyFlow

typealias IngredientDecider = Decider<IngredientCommand?, Ingredient?, IngredientEvent?>

fun ingredientDecider(): IngredientDecider = Decider(
    initialState = null,
    decide = { c, s ->
        when (c) {
            null -> emptyFlow()
            else -> TODO()
        }
    },
    evolve = { s, e ->
        when (e) {
            null -> s
            else -> TODO()
        }
    }
)


data class Ingredient(
    val id: IngredientId,
    val name: IngredientName,
    val quantity: IngredientQuantity,
    val ingredientInputTime: IngredientInputTime
)

