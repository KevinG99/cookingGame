package com.cookingGame.domain

import com.cookingGame.LOGGER
import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration


typealias GameDecider = Decider<GameCommand?, Game?, GameEvent?>


fun gameDecider() = GameDecider(
    initialState = null,
    decide = { gameCommand: GameCommand?, game: Game? ->
        when (gameCommand) {
            is CheckGameTimerCommand ->
                when {
                    game == null -> flowOf(
                        GameDoesNotExistEvent(
                            gameCommand.identifier,
                            Error.GameDoesNotExist.reason,
                            true
                        )
                    )

                    GameStatus.STARTED != game.status -> flowOf(
                        GameNotInStartedStateEvent(
                            gameCommand.identifier,
                            Error.GameNotStarted.reason,
                            true
                        )
                    )
                    else -> game.startGameTimer()
                }

            null -> emptyFlow()
            is CreateGameCommand ->
                if (game == null) flowOf(GameCreatedEvent(gameCommand.identifier, gameCommand.name))
                else flowOf(GameAlreadyExistsEvent(gameCommand.identifier, Error.GameAlreadyExists.reason, true))

            is PrepareGameCommand ->
                if (game == null) flowOf(
                    GameDoesNotExistEvent(
                        gameCommand.identifier,
                        Error.GameDoesNotExist.reason,
                        true
                    )
                )
                else if (GameStatus.CREATED != game.status) flowOf(
                    GameNotInCreatableStateEvent(
                        gameCommand.identifier,

                        Error.GameNotCreated.reason,
                        true
                    )
                )
                else flowOf(
                    GamePreparedEvent(
                        gameCommand.identifier,
                        gameCommand.ingredients,
                        gameCommand.gameDuration
                    )
                )

            is StartGameCommand ->
                if (game == null) flowOf(
                    GameDoesNotExistEvent(
                        gameCommand.identifier,
                        Error.GameDoesNotExist.reason,
                        true
                    )
                )
                else if (GameStatus.PREPARED != game.status) flowOf(
                    GameNotInPreparedStateEvent(
                        gameCommand.identifier,

                        Error.GameNotPrepared.reason,
                        true
                    )
                )
                else flowOf(
                    GameStartedEvent(
                        gameCommand.identifier,
                        gameCommand.ingredients,
                        gameCommand.startTime,
                        gameCommand.gameDuration
                    )
                )


            is CompleteGameCommand ->
                if (game == null) flowOf(
                    GameDoesNotExistEvent(
                        gameCommand.identifier,
                        Error.GameDoesNotExist.reason,
                        true
                    )
                )
                else if (GameStatus.STARTED != game.status) flowOf(
                    GameNotInStartedStateEvent(
                        gameCommand.identifier,
                        Error.GameNotStarted.reason,
                        true
                    )
                )
                else if(game.isSuccess?.value == false){
                    flowOf(
                        GameCompletedEvent(
                            gameCommand.identifier,
                            gameCommand.completionTime,
                            gameCommand.isSuccess,
                            game.score ?: GameScore(0)
                        )
                    )
                }
                else flowOf(
                    GameCompletedEvent(
                        gameCommand.identifier,
                        gameCommand.completionTime,
                        gameCommand.isSuccess,
                        game.score ?: GameScore(0)
                    )
                )
        }
    },
    evolve = { game, gameEvent ->
        when (gameEvent) {
            null -> game
            is GameCreatedEvent -> Game(gameEvent.identifier, gameEvent.name, gameEvent.status)
            is GamePreparedEvent -> game?.copy(status = gameEvent.status, ingredients = gameEvent.ingredients, gameDuration = gameEvent.gameDuration)
            is GameStartedEvent -> game?.copy(
                status = gameEvent.status,
                startTime = gameEvent.startTime,
            )

            is GameTimeElapsedEvent -> game?.copy(isSuccess = Success(false))
            is GameCompletedEvent -> game?.copy(status = gameEvent.status, isSuccess = gameEvent.isSuccess)
            is GameAlreadyExistsEvent -> game
            is GameNotInCreatableStateEvent -> game
            is GameDoesNotExistEvent -> game
            is GameNotInPreparedStateEvent -> game
            is GameNotInStartedStateEvent -> game
        }

    }
)

private fun Game.startGameTimer(): Flow<GameEvent> = callbackFlow {
    val endTime = startTime?.value?.plus(gameDuration?.value?.longValueExact()!!, DateTimeUnit.SECOND)!!

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val job = scope.launch {
        LOGGER.info("End time: $endTime")
        var clock = Clock.System.now()
        LOGGER.debug("Start time: {}", clock)
        try {
            while (isActive && clock < endTime) {
                clock = Clock.System.now()
                if (clock > endTime) break
            }
            send(GameTimeElapsedEvent(id))
        } catch (e: CancellationException) {
            LOGGER.error("Timer canceled: $id", e)
        } finally {
            scope.cancel()
        }
    }

    awaitClose { job.cancel() }
}

private fun Game.isTimeElapsed(): Boolean =
    (gameDuration?.value?.toPlainString()?.let { Duration.parse(it) }?.let { startTime?.value?.plus(it) }
        ?: Instant.DISTANT_PAST) < Clock.System.now()

data class Game(
    val id: GameId,
    val name: GameName,
    val status: GameStatus,
    val gameDuration: GameDuration? = null,
    val ingredients: IngredientList? = null,
    val startTime: GameStartTime? = null,
    val score: GameScore? = null,
    val isSuccess: Success? = null
)