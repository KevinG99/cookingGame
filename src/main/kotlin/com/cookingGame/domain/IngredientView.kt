package com.cookingGame.domain

import com.fraktalio.fmodel.domain.View

typealias IngredientView = View<IngredientViewState?, IngredientEvent?>

fun ingredientView() = IngredientView(
    initialState = null,
    evolve = { ingredientViewState, ingredientEvent ->
        when (ingredientEvent) {
            null -> ingredientViewState
            is IngredientInitializedEvent -> IngredientViewState(
                ingredientEvent.identifier,
                ingredientEvent.gameId,
                ingredientEvent.ingredientName,
                ingredientEvent.ingredientQuantity,
                ingredientEvent.inputTime,
                ingredientEvent.status
            )
            is IngredientAlreadyExistsEvent -> ingredientViewState
        }
    }
)
data class IngredientViewState(
    val id: IngredientId,
    val gameId: GameId,
    val name: IngredientName,
    val quantity: IngredientQuantity,
    val inputTime: IngredientInputTime,
    val status: IngredientStatus
)