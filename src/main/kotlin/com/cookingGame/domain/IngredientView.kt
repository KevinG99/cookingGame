package com.cookingGame.domain

import com.fraktalio.fmodel.domain.View
import kotlinx.collections.immutable.toImmutableList

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
            is IngredientDoesNotExistEvent -> ingredientViewState
            is IngredientNotInCorrectStateEvent -> ingredientViewState
            is IngredientPreparedEvent -> ingredientViewState?.copy(status = ingredientEvent.status ,preparationTimestamps = addIngredientPreparationTimeStamp(ingredientViewState.preparationTimestamps, ingredientEvent.preparationTimeStamp))
            is IngredientAddedEvent -> ingredientViewState?.copy(status = IngredientStatus.ADDED, addedTimestamps = addIngredientAddedTimeStamp(ingredientViewState.addedTimestamps, ingredientEvent.addedTimestamp))
        }
    }
)
data class IngredientViewState(
    val id: IngredientId,
    val gameId: GameId,
    val name: IngredientName,
    val quantity: IngredientQuantity,
    val inputTime: IngredientInputTime,
    val status: IngredientStatus,
    val preparationTimestamps: IngredientPreparationList = IngredientPreparationList(),
    val addedTimestamps: IngredientAddedList = IngredientAddedList()
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
