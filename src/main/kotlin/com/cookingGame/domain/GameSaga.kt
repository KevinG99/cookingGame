package com.cookingGame.domain

import com.cookingGame.adapter.clients.GameClient
import com.fraktalio.fmodel.domain.Saga
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf

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
fun gameSaga(gameClient: GameClient) = GameSaga(
    react = { e ->
        when (e) {
            null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
            is GamePreparationStartedEvent -> gameClient.getIngredients(e.name).flatMapConcat { ingredientList ->
                flowOf(GenerateGameCommand(e.identifier, e.name, ingredientList))
            }
            is GameGeneratedEvent -> emptyFlow()
            is GameNotCreatedEvent -> emptyFlow()
            is GameStartedEvent -> emptyFlow()
        }
    }
)
