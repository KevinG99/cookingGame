package com.cookingGame.domain

import com.fraktalio.fmodel.domain.View
import kotlinx.serialization.Serializable

typealias GameView = View<GameViewState?, GameEvent?>

fun gameView() = GameView(
    initialState = null,
    evolve = { s, e ->
        when (e) {
            null -> s
            is GameCreatedEvent -> GameViewState(
                e.identifier,
                e.name,
                e.status,
            )

            is GamePreparedEvent -> s?.copy(status = e.status, ingredients = e.ingredients, gameDuration = e.gameDuration)
            is GameStartedEvent -> s?.copy(status = e.status, startTime = e.startTime)
            is GameTimeElapsedEvent -> s?.copy(status = e.status)
            is GameEndedEvent -> s?.copy(status = e.status,score = e.score, completionTime = e.completionTime)
            is GameCompletedEvent -> s?.copy(status = e.status , isSuccess = e.isSuccess)
            is GameErrorEvent -> s
        }
    }
)

@Serializable
data class GameViewState(
    val id: GameId,
    val name: GameName,
    val status: GameStatus,
    val ingredients: IngredientList? = null,
    val gameDuration: GameDuration? = null,
    val startTime: GameStartTime? = null,
    val isSuccess: Success? = null,
    val score: GameScore? = null,
    val completionTime: GameCompletionTime? = null,
)