package com.cookingGame.domain

import com.cookingGame.LOGGER
import com.cookingGame.adapter.clients.GameClient
import com.cookingGame.application.GameService
import com.fraktalio.fmodel.domain.Saga
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

/**
 * A convenient type alias for Saga<GameEvent?, GameCommand>
 */
typealias GameSaga = Saga<GameEvent?, GameCommand>

/**
 * Saga is a datatype that represents the central point of control deciding what to execute next.
 * It is responsible for mapping different events from aggregates into action results (AR) that the [Saga] then can use to calculate the next actions (A) to be mapped to command of other aggregates.
 *
 * Saga does not maintain the state.
 *
 * `react` is a pure function/lambda that takes any event/action-result of type [GameEvent] as parameter, and returns the flow of commands/actions Flow<[GameCommand]> to be published further downstream.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun gameSaga(gameClient: GameClient, gameService: GameService) = GameSaga(
    react = { e ->
        when (e) {
            is GameStartedEvent -> {
                gameService.startGameTimer(e.identifier, e.startTime, e.gameDuration)
            }

            null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
            is GameCreatedEvent -> gameClient.getIngredients(e.name).flatMapConcat { ollamaResponse ->
                flowOf(PrepareGameCommand(e.identifier, ollamaResponse.ingredientList, ollamaResponse.gameDuration))
            }

            is GamePreparedEvent -> emptyFlow()
            is GameAlreadyExistsEvent -> emptyFlow()

            is GameDoesNotExistEvent -> emptyFlow()
            is GameNotInCreatableStateEvent -> emptyFlow()
            is GameNotInPreparedStateEvent -> emptyFlow()
            is GameCompletedEvent -> TODO()
            is GameNotInStartedStateEvent -> TODO()
            is GameTimeElapsedEvent -> flowOf(CompleteGameCommand(e.identifier, GameCompletionTime(), Success(false)))
        }
    }
)

suspend fun processGameStartEvents(gameStartEvents: Flow<GameStartedEvent>) {
    coroutineScope {
        gameStartEvents.collect { gameStartEvent ->
            launch {
                startGameTimer(gameStartEvent.identifier, gameStartEvent.startTime, gameStartEvent.gameDuration)
                    .collect { gameCommand ->
                        LOGGER.info("Game command received: $gameCommand")
                    }
            }
        }
    }
}

fun startGameTimer(gameId: GameId, startTime: GameStartTime, gameDuration: GameDuration): Flow<GameCommand> = flow {
    val endTime = startTime.value.plus(gameDuration.value.longValueExact(), DateTimeUnit.SECOND)
    LOGGER.info("End time: $endTime")
    var clock = Clock.System.now()
    while (clock < endTime) {
        clock = Clock.System.now()
        if (clock > endTime) break
    }
    emit(CheckGameTimerCommand(gameId))
    LOGGER.info("Command emitted: $clock")
}