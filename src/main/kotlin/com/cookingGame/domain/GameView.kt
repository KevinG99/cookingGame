package com.cookingGame.domain

import com.fraktalio.fmodel.domain.View
import kotlinx.serialization.Serializable

typealias GameView = View<GameViewState?, GameEvent?>

fun gameView() = GameView(
    initialState = null,
    evolve = { s, e ->
        when (e) {
            is GameCreatedEvent -> GameViewState(
                e.identifier,
                e.name,
                e.status,
            )

            is GamePreparedEvent -> s?.copy(status = e.status, ingredients = e.ingredients, gameDuration = e.gameDuration)
            is GameStartedEvent -> s?.copy(status = e.status, startTime = e.startTime)
            is GameErrorEvent -> s
            null -> s
        }
    }
)

@Serializable
data class GameViewState(
    val id: GameId,
    val name: GameName,
    val status: GameStatus,
    val ingredients: IngredientList? = null,
    val startTime: GameStartTime? = null,
    val gameDuration: GameDuration? = null,
)