package com.cookingGame.domain

import com.fraktalio.fmodel.domain.Decider
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

typealias IngredientDecider = Decider<IngredientCommand?, Ingredient?, IngredientEvent?>

fun ingredientDecider(): IngredientDecider = Decider(
    initialState = null,
    decide = { ingredientCommand, ingredient ->
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
            else flowOf(
                IngredientAlreadyExistsEvent(
                    ingredientCommand.identifier,
                    Error.IngredientAlreadyExists.reason,
                    true
                )
            )

            is PrepareIngredientCommand -> if (ingredient == null) flowOf(
                IngredientDoesNotExistEvent(
                    ingredientCommand.identifier,
                    Error.IngredientDoesNotExist.reason
                )
            )
            else if (IngredientStatus.INITIALIZED != ingredient.status) flowOf(
                IngredientNotInCorrectStateEvent(
                    ingredientCommand.identifier,
                    Error.IngredientNotInCorrectState.reason,
                    ingredient.status
                )
            )
            else flowOf(IngredientPreparedEvent(ingredientCommand.identifier))

            is AddIngredientCommand -> if (ingredient == null) flowOf(
                IngredientDoesNotExistEvent(
                    ingredientCommand.identifier,
                    Error.IngredientDoesNotExist.reason
                )
            )
            else if (IngredientStatus.PREPARED != ingredient.status) flowOf(
                IngredientNotInCorrectStateEvent(
                    ingredientCommand.identifier,
                    Error.IngredientNotInCorrectState.reason,
                    ingredient.status
                )
            )
            else flowOf(
                IngredientAddedEvent(
                    ingredientCommand.identifier,
                )
            )
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
            is IngredientDoesNotExistEvent -> ingredient
            is IngredientNotInCorrectStateEvent -> ingredient
            is IngredientPreparedEvent -> ingredient?.copy(status = ingredientEvent.status ,preparationTimestamps = addIngredientPreparationTimeStamp(ingredient.preparationTimestamps, ingredientEvent.preparationTimeStamp))
            is IngredientAddedEvent -> ingredient?.copy(status = IngredientStatus.ADDED, addedTimestamp = addIngredientAddedTimeStamp(ingredient.addedTimestamp, ingredientEvent.addedTimestamp))
        }
    }
)

data class Ingredient(
    val id: IngredientId,
    val gameId: GameId,
    val name: IngredientName,
    val quantity: IngredientQuantity,
    val inputTime: IngredientInputTime,
    val status: IngredientStatus,
    val preparationTimestamps: IngredientPreparationList = IngredientPreparationList(),
    val addedTimestamp: IngredientAddedList = IngredientAddedList()
)

private fun addIngredientPreparationTimeStamp(
    currentList: IngredientPreparationList,
    ingredientPreparationTimestamp: IngredientPreparationTimestamp
): IngredientPreparationList {
    val newList = currentList.value.toMutableList().apply {
        add(ingredientPreparationTimestamp)
    }
    return IngredientPreparationList(newList.toImmutableList())
}

private fun addIngredientAddedTimeStamp(
    currentList: IngredientAddedList,
    ingredientAddedTimestamp: IngredientAddedTimestamp
): IngredientAddedList {
    val newList = currentList.value.toMutableList().apply {
        add(ingredientAddedTimestamp)
    }
    return IngredientAddedList(newList.toImmutableList())
}
