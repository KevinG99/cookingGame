package com.cookingGame.domain

import com.cookingGame.LOGGER
import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.coroutines.cancellation.CancellationException


typealias GameDecider = Decider<GameCommand?, Game?, GameEvent?>


fun gameDecider() = GameDecider(
    initialState = null,
    decide = { gameCommand: GameCommand?, game: Game? ->
        when (gameCommand) {
            null -> emptyFlow()
            is CreateGameCommand ->
                if (game == null) flowOf(GameCreatedEvent(gameCommand.identifier, gameCommand.name))
                else flowOf(GameAlreadyExistsEvent(gameCommand.identifier, Error.GameAlreadyExists.reason, true))

            is PrepareGameCommand ->
                if (game == null) flowOf(
                    GameDoesNotExistEvent(gameCommand.identifier, Error.GameDoesNotExist.reason, true)
                )
                else if (GameStatus.CREATED != game.status) flowOf(
                    GameNotInCorrectState(
                        gameCommand.identifier,
                        Error.GameNotInCorrectState.reason,
                        game.status,
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
                    GameNotInCorrectState(
                        gameCommand.identifier,
                        Error.GameNotInCorrectState.reason,
                        game.status,
                        true
                    )
                )
                else flowOf(
                    GameStartedEvent(
                        gameCommand.identifier,
                        gameCommand.ingredients
                    )
                )


            is StartGameTimerCommand ->
                when {
                    game == null -> flowOf(
                        GameDoesNotExistEvent(
                            gameCommand.identifier,
                            Error.GameDoesNotExist.reason,
                            true
                        )
                    )

                    GameStatus.STARTED != game.status -> flowOf(
                        GameNotInCorrectState(
                            gameCommand.identifier,
                            Error.GameNotInCorrectState.reason,
                            game.status,
                            true
                        )
                    )

                    else -> GameTimerManager.startTimer(game)
                }

            is CompleteGameCommand ->
                if (game == null) flowOf(
                    GameDoesNotExistEvent(
                        gameCommand.identifier,
                        Error.GameDoesNotExist.reason,
                        true
                    )
                )
                else if (GameStatus.GAME_OVER == game.status) flowOf(
                    GameCompletedEvent(
                        gameCommand.identifier,
                        Success(false),
                    )
                )
                else if (GameStatus.GAME_ENDED == game.status) flowOf(
                    GameCompletedEvent(
                        gameCommand.identifier,
                        Success(true),
                    )
                )
                else flowOf(
                    GameNotInCorrectState(
                        gameCommand.identifier,
                        Error.GameNotInCorrectState.reason,
                        game.status,
                        true
                    )
                )

            is EndGameCommand -> TODO()
        }
    },
    evolve = { game, gameEvent ->
        when (gameEvent) {
            null -> game
            is GameCreatedEvent -> Game(gameEvent.identifier, gameEvent.name, gameEvent.status)
            is GamePreparedEvent -> game?.copy(
                status = gameEvent.status,
                ingredients = gameEvent.ingredients,
                gameDuration = gameEvent.gameDuration
            )

            is GameStartedEvent -> game?.copy(
                status = gameEvent.status,
                startTime = gameEvent.startTime,
            )

            is GameTimeElapsedEvent -> game?.copy(
                status = gameEvent.status
            )

            is GameEndedEvent -> TODO()
            is GameCompletedEvent -> game?.copy(status = gameEvent.status, isSuccess = gameEvent.isSuccess)
            is GameAlreadyExistsEvent -> game
            is GameDoesNotExistEvent -> game
            is GameNotInCorrectState -> game
        }

    }
)


object GameTimerManager {
    private val activeTimers = mutableMapOf<GameId, Job>()

    fun startTimer(game: Game): Flow<GameEvent> = flow {
        val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val endTime = game.startTime?.value?.plus(game.gameDuration?.value?.toLong()!!, DateTimeUnit.SECOND)!!
        val channel = Channel<GameEvent>()

        val job = timerScope.launch {
            LOGGER.info("Start time: ${game.startTime.value}")
            LOGGER.info("End time: $endTime")
            val gameId = game.id
            var clock = Clock.System.now()
            try {
                while (isActive && clock < endTime) {
                    clock = Clock.System.now()
                    if (clock > endTime) break
                }
                channel.send(GameTimeElapsedEvent(gameId))
                LOGGER.info("Command emitted: $clock, ${gameId.value}")
                channel.close()
            } catch (e: CancellationException) {
                LOGGER.error("Timer canceled: $gameId", e)
                channel.close(e)
            } finally {
                activeTimers.remove(gameId)
            }
        }

        activeTimers[game.id] = job
        job.invokeOnCompletion { activeTimers.remove(game.id) }

        channel.consumeAsFlow().collect { emit(it) }
    }

    fun stopTimer(gameId: GameId) {
        activeTimers[gameId]?.cancel(CancellationException("Game timer stopped"))
    }
}

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