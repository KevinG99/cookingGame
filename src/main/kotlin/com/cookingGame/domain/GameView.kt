package com.cookingGame.domain

import com.fraktalio.fmodel.domain.View
import kotlinx.serialization.Serializable

typealias GameView = View<GameViewState?, GameEvent?>

fun gameView() = GameView(
    initialState = null,
    evolve = { gameViewState, gameEvent ->
        when (gameEvent) {
            null -> gameViewState
            is GameCreatedEvent -> GameViewState(
                gameEvent.identifier,
                gameEvent.name,
                gameEvent.status,
            )

            is GamePreparedEvent -> gameViewState?.copy(status = gameEvent.status, ingredients = gameEvent.ingredients, gameDuration = gameEvent.gameDuration)
            is GameStartedEvent -> gameViewState?.copy(status = gameEvent.status, startTime = gameEvent.startTime)
            is GameTimeElapsedEvent -> gameViewState?.copy(status = gameEvent.status)
            is GameEndedEvent -> gameViewState?.copy(status = gameEvent.status,score = gameEvent.score, completionTime = gameEvent.completionTime)
            is GameCompletedEvent -> gameViewState?.copy(status = gameEvent.status , isSuccess = gameEvent.isSuccess)
            is GameErrorEvent -> gameViewState
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