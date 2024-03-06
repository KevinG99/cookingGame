package com.cookingGame.domain

import com.fraktalio.fmodel.domain.View
import kotlinx.collections.immutable.ImmutableList

typealias GameView = View<GameViewState?, GameEvent?>

fun gameView() = GameView(
    initialState = null,
    evolve = { s, e ->
        when (e) {
            is GamePreparationStartedEvent -> GameViewState(e.identifier, e.name)
            is GameGeneratedEvent -> s?.copy(name = e.name, ingredients = e.ingredients)
            is GameStartedEvent -> TODO()
            is GameErrorEvent -> s
            null -> s
        }
    }
)


data class GameViewState(
    val id: GameId,
    val name: GameName,
    val ingredients: ImmutableList<IngredientItem>? = null
)