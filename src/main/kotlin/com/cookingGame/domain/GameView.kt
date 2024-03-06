package com.cookingGame.domain

import com.fraktalio.fmodel.domain.View
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

typealias GameView = View<GameViewState?, GameEvent?>

fun gameView() = GameView(
    initialState = null,
    evolve = { s, e ->
        when (e) {
            is GameCreatedEvent -> GameViewState(e.identifier, e.name, e.status)
            is GamePreparedEvent -> s?.copy(name = e.name, ingredients = e.ingredients)
            is GameStartedEvent -> TODO()
            is GameErrorEvent -> s
            null -> s
        }
    }
)


data class GameViewState(
    val id: GameId,
    val name: GameName,
    val status: GameStatus,
    val ingredients: ImmutableList<IngredientItem> = emptyList<IngredientItem>().toImmutableList()
)