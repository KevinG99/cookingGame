package com.cookingGame.domain

import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf


typealias GameDecider = Decider<GameCommand?, Game?, GameEvent?>


fun gameDecider() = GameDecider(
    initialState = null,
    decide = { c: GameCommand?, s: Game? ->
        when (c) {
            is CreateGameCommand ->
                if (s == null) flowOf(GameCreatedEvent(c.identifier, c.name))
                else flowOf(GameAlreadyExistsEvent(c.identifier, c.name, Error.GameAlreadyExists.reason, true))

            is PrepareGameCommand ->
                if (s == null) flowOf(GameDoesNotExistEvent(c.identifier, c.name, Error.GameDoesNotExist.reason, true))
                else if(GameStatus.CREATED != s.status) flowOf(GameNotInCreatableStateEvent(c.identifier, c.name, Error.GameNotCreated.reason, true))
                else flowOf(GamePreparedEvent(c.identifier, c.name, c.ingredients, c.gameDuration))

            is StartGameCommand ->
                if (s == null) flowOf(GameDoesNotExistEvent(c.identifier, c.name, Error.GameDoesNotExist.reason, true))
                else if(GameStatus.PREPARED != s.status) flowOf(GameNotInPreparedStateEvent(c.identifier, c.name, Error.GameNotPrepared.reason, true))
                else flowOf(GameStartedEvent(c.identifier, c.name, c.ingredients, c.startTime, c.gameDuration))

            is StartTimerCommand -> TODO()
            null -> emptyFlow()
        }
    },
    evolve = { s, e ->
        when (e) {
            is GameCreatedEvent -> Game(e.identifier, e.name, e.status)
            is GamePreparedEvent -> s?.copy(status = e.status, ingredients = e.ingredients)
            is GameStartedEvent -> s?.copy(status= e.status, startTime = e.startTime, gameDuration = e.gameDuration)
            is GameAlreadyExistsEvent -> s
            is GameNotInCreatableStateEvent -> s
            is GameDoesNotExistEvent -> s
            is GameNotInPreparedStateEvent -> s
            null -> s
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
)