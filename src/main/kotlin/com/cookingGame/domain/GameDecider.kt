package com.cookingGame.domain

import com.cookingGame.LOGGER
import com.fraktalio.fmodel.domain.Decider
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus


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

            is EndGameCommand -> {
                if (game == null) flowOf(
                    GameDoesNotExistEvent(
                        gameCommand.identifier,
                        Error.GameDoesNotExist.reason,
                        true
                    )
                )
                else if (GameStatus.STARTED != game.status) flowOf(
                    GameNotInCorrectState(
                        gameCommand.identifier,
                        Error.GameNotInCorrectState.reason,
                        game.status,
                        true
                    )
                )
                else flowOf(
                    GameEndedEvent(
                        gameCommand.identifier,
                        gameCommand.score
                    )
                )
            }

            is UpdateGameIngredientCommand -> if (game == null) flowOf(
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
            else if (game.ingredients?.value?.none { it.id == gameCommand.ingredientId } == true) flowOf(
                GameDoesNotContainIngredientEvent(
                    gameCommand.identifier,
                    gameCommand.ingredientId,
                    Error.GameDoesNotHaveIngredient.reason,
                    true
                )
            )
            else flowOf(
                GameIngredientUpdatedEvent(
                    gameCommand.identifier,
                    gameCommand.ingredientId,
                    gameCommand.ingredientStatus
                )
            )

            is CompleteIngredientPreparationCommand -> if (game == null) flowOf(
                GameDoesNotExistEvent(
                    gameCommand.identifier,
                    Error.GameDoesNotExist.reason,
                    true
                )
            )
            else if (GameStatus.STARTED != game.status) flowOf(
                GameNotInCorrectState(
                    gameCommand.identifier,
                    Error.GameNotInCorrectState.reason,
                    game.status,
                    true
                )
            )
            else if (game.ingredients?.value?.none { it.id == gameCommand.ingredientId } == true) flowOf(
                GameDoesNotContainIngredientEvent(
                    gameCommand.identifier,
                    gameCommand.ingredientId,
                    Error.GameDoesNotHaveIngredient.reason,
                    true
                )
            )
            else flowOf(
                IngredientPreparationCompletedEvent(
                    gameCommand.identifier,
                    gameCommand.ingredientId
                )
            )

            is AddIngredientToGameCommand -> if (game == null) flowOf(
                GameDoesNotExistEvent(
                    gameCommand.identifier,
                    Error.GameDoesNotExist.reason,
                    true
                )
            )
            else if (GameStatus.STARTED != game.status) flowOf(
                GameNotInCorrectState(
                    gameCommand.identifier,
                    Error.GameNotInCorrectState.reason,
                    game.status,
                    true
                )
            )
            else if (game.ingredients?.value?.none { it.id == gameCommand.ingredientId } == true) flowOf(
                GameDoesNotContainIngredientEvent(
                    gameCommand.identifier,
                    gameCommand.ingredientId,
                    Error.GameDoesNotHaveIngredient.reason,
                    true
                )
            )
            else flowOf(
                GameIngredientAdditionCompletedEvent(
                    gameCommand.identifier,
                    gameCommand.ingredientId
                )
            )

            is StopGameCommand -> if (game == null) flowOf(
                GameDoesNotExistEvent(
                    gameCommand.identifier,
                    Error.GameDoesNotExist.reason,
                    true
                )
            )
            else if (GameStatus.STARTED != game.status) flowOf(
                GameNotInCorrectState(
                    gameCommand.identifier,
                    Error.GameNotInCorrectState.reason,
                    game.status,
                    true
                )
            )
            else GameTimerManager.stopTimer(gameCommand.identifier)
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

            is GameEndedEvent -> game?.copy(
                status = gameEvent.status,
                score = gameEvent.score,
                completionTime = gameEvent.completionTime
            )

            is GameCompletedEvent -> game?.copy(status = gameEvent.status, isSuccess = gameEvent.isSuccess)
            is GameAlreadyExistsEvent -> game
            is GameDoesNotExistEvent -> game
            is GameNotInCorrectState -> game
            is GameIngredientUpdatedEvent -> {
                game?.let { state ->
                    state.copy(
                        ingredients = IngredientList(
                            state.ingredients?.value?.map {
                                if (it.id == gameEvent.ingredientId) it.copy(status = gameEvent.ingredientStatus) else it
                            }?.toImmutableList()!!
                        )
                    )
                }
            }

            is GameDoesNotContainIngredientEvent -> game
            is IngredientPreparationCompletedEvent -> {
                game?.let { state ->
                    state.copy(
                        ingredients = IngredientList(
                            state.ingredients?.value?.map { ingredientItem ->
                                if (ingredientItem.id == gameEvent.ingredientId) {
                                    ingredientItem.copy(
                                        status = gameEvent.ingredientStatus,
                                        preparationCompleteTime = gameEvent.preparationCompleteTime
                                    )
                                } else ingredientItem
                            }?.toImmutableList()!!
                        )
                    )
                }
            }

            is GameIngredientAdditionCompletedEvent -> {
                game?.let { state ->
                    state.copy(
                        ingredients = IngredientList(
                            state.ingredients?.value?.map { ingredientItem ->
                                if (ingredientItem.id == gameEvent.ingredientId) {
                                    ingredientItem.copy(
                                        status = gameEvent.ingredientStatus,
                                        additionCompleteTime = gameEvent.additionCompletedTimestamp
                                    )
                                } else ingredientItem
                            }?.toImmutableList()!!
                        )
                    )
                }
            }

            is GameStoppedEvent -> game
        }
    }
)


data class Game(
    val id: GameId,
    val name: GameName,
    val status: GameStatus,
    val gameDuration: GameDuration? = null,
    val ingredients: IngredientList? = null,
    val startTime: GameStartTime? = null,
    val score: GameScore? = null,
    val isSuccess: Success? = null,
    val completionTime: GameCompletionTime? = null
)

object GameTimerManager {
    private val activeTimers = mutableMapOf<GameId, Job>()

    fun startTimer(game: Game): Flow<GameEvent> = flow {
        val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val endTime = game.startTime?.value?.plus(game.gameDuration?.value?.toLong()!!, DateTimeUnit.SECOND)!!
        val channel = Channel<GameEvent>()

        val job = timerScope.launch {
            LOGGER.debug("Start time: ${game.startTime.value}")
            LOGGER.debug("End time: $endTime")
            val gameId = game.id
            while (isActive && Clock.System.now() < endTime) {
            }

            if (isActive && Clock.System.now() > endTime) {
                channel.send(GameTimeElapsedEvent(gameId))
                LOGGER.debug("Command emitted:, ${gameId.value}")
            }
            channel.close()
        }

        activeTimers[game.id] = job
        job.invokeOnCompletion {
            LOGGER.debug("Timer job completed: ${game.id}, $it")
            activeTimers.remove(game.id)
        }

        channel.consumeAsFlow().collect { emit(it) }
    }

    fun stopTimer(gameId: GameId) = flow{
        LOGGER.debug("Attempting to stop timer for gameId: $gameId, ${activeTimers[gameId]}")
        activeTimers[gameId]?.cancel() ?: LOGGER.error("Timer not found: $gameId")
        LOGGER.debug("Timer stopped: $gameId, ${activeTimers[gameId]}")
        emit(GameStoppedEvent(gameId))
    }
}
