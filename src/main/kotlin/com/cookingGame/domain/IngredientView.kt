package com.cookingGame.domain

import com.fraktalio.fmodel.domain.View

typealias IngredientView = View<IngredientViewState?, IngredientEvent?>

fun ingredientView() = IngredientView(
    initialState = null,
    evolve = { s, e ->
        when (e) {
            null -> s
            else -> s //TODO
        }
    }
)
data class IngredientViewState(
    val id: IngredientId,
    val name: IngredientName,
    val quantity: IngredientQuantity,
    val ingredientInputTime: IngredientInputTime
)