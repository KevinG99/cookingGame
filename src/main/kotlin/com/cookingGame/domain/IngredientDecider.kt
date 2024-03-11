package com.cookingGame.domain

import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

typealias IngredientDecider = Decider<IngredientCommand?, Ingredient?, IngredientEvent?>

fun ingredientDecider(): IngredientDecider = Decider(
    initialState = null,
    decide = { ingredientCommand, ingredient  ->
        when (ingredientCommand) {
            null -> emptyFlow()
            is InitalizeIngredientCommand -> if (ingredient == null) flowOf(
                IngredientInitializedEvent(
                    ingredientCommand.identifier,
                    ingredientCommand.gameId,
                    ingredientCommand.name,
                    ingredientCommand.quantity,
                    ingredientCommand.inputTime
                )
            )
            else flowOf(IngredientAlreadyExistsEvent(ingredientCommand.identifier, Reason("Ingredient already exists")))
        }
    },
    evolve = { ingredient, ingredientEvent ->
        when (ingredientEvent) {
            null -> ingredient
            is IngredientInitializedEvent -> Ingredient(
                ingredientEvent.identifier,
                ingredientEvent.gameId,
                ingredientEvent.ingredientName,
                ingredientEvent.ingredientQuantity,
                ingredientEvent.inputTime,
                ingredientEvent.status
            )

            is IngredientAlreadyExistsEvent -> ingredient
        }
    }
)


data class Ingredient(
    val id: IngredientId,
    val gameId: GameId,
    val name: IngredientName,
    val quantity: IngredientQuantity,
    val inputTime: IngredientInputTime,
    val status: IngredientStatus
)

