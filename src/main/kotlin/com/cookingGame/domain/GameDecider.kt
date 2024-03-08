package com.cookingGame.domain

import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration


typealias GameDecider = Decider<GameCommand?, Game?, GameEvent?>


fun gameDecider() = GameDecider(
    initialState = null,
    decide = { gameCommand: GameCommand?, game: Game? ->
        when (gameCommand) {
            is CreateGameCommand ->
                if (game == null) flowOf(GameCreatedEvent(gameCommand.identifier, gameCommand.name))
                else flowOf(GameAlreadyExistsEvent(gameCommand.identifier, Error.GameAlreadyExists.reason, true))

            is PrepareGameCommand ->
                if (game == null) flowOf(GameDoesNotExistEvent(gameCommand.identifier, Error.GameDoesNotExist.reason, true))
                else if (GameStatus.CREATED != game.status) flowOf(
                    GameNotInCreatableStateEvent(
                        gameCommand.identifier,

                        Error.GameNotCreated.reason,
                        true
                    )
                )
                else flowOf(GamePreparedEvent(gameCommand.identifier, gameCommand.ingredients, gameCommand.gameDuration))

            is StartGameCommand ->
                if (game == null) flowOf(GameDoesNotExistEvent(gameCommand.identifier, Error.GameDoesNotExist.reason, true))
                else if (GameStatus.PREPARED != game.status) flowOf(
                    GameNotInPreparedStateEvent(
                        gameCommand.identifier,

                        Error.GameNotPrepared.reason,
                        true
                    )
                )
                else flowOf(GameStartedEvent(gameCommand.identifier, gameCommand.ingredients, gameCommand.startTime, gameCommand.gameDuration))

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

                    game.isTimeElapsed() -> flowOf(GameTimeElapsedEvent(gameCommand.identifier))
                    else -> emptyFlow()
                }

            is CompleteGameCommand -> TODO()

            null -> emptyFlow()
        }
    },
    evolve = { game, gameEvent ->
        when (gameEvent) {
            null -> game
            is GameCreatedEvent -> Game(gameEvent.identifier, gameEvent.name, gameEvent.status)
            is GamePreparedEvent -> game?.copy(status = gameEvent.status, ingredients = gameEvent.ingredients)
            is GameStartedEvent -> game?.copy(status = gameEvent.status, startTime = gameEvent.startTime, gameDuration = gameEvent.gameDuration)
            is GameAlreadyExistsEvent -> game
            is GameNotInCreatableStateEvent -> game
            is GameDoesNotExistEvent -> game
            is GameNotInPreparedStateEvent -> game
            is GameCompletedEvent -> TODO()
            is GameNotInStartedStateEvent -> game
            is GameTimeElapsedEvent -> TODO()
        }

    }
)

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
)