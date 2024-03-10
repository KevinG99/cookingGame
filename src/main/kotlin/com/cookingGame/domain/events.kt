package com.cookingGame.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class Event {
    abstract val final: Boolean
}

@Serializable
sealed class GameEvent : Event() {
    abstract val identifier: GameId
}
@Serializable
sealed class IngredientEvent : Event() {
    abstract val identifier: IngredientId
}

@Serializable
sealed class GameErrorEvent : GameEvent() {
    abstract val reason: Reason
}

@Serializable
data class GameCreatedEvent(
    override val identifier: GameId,
    val name: GameName,
    override val final: Boolean = false
) : GameEvent() {
    val status = GameStatus.CREATED
}

@Serializable
data class GamePreparedEvent(
    override val identifier: GameId,
    val ingredients: IngredientList,
    val gameDuration: GameDuration,
    override val final: Boolean = false
) : GameEvent(){
    val status = GameStatus.PREPARED
}


@Serializable
data class GameStartedEvent(
    override val identifier: GameId,
    val ingredients : IngredientList,
    override val final: Boolean = false
) : GameEvent(){
    val status = GameStatus.STARTED
    val startTime : GameStartTime = GameStartTime()
}

@Serializable
data class GameTimeElapsedEvent(
    override val identifier: GameId,
    override val final: Boolean = false
) : GameEvent(){
    val status = GameStatus.GAME_OVER
    val isSuccess = Success(false)
}

@Serializable
data class GameEndedEvent(
    override val identifier: GameId,
    val score : GameScore,
    val completionTime: GameCompletionTime,
    override val final: Boolean = false
) : GameEvent(){
    val status = GameStatus.GAME_OVER
}

@Serializable
data class GameCompletedEvent(
    override val identifier: GameId,
    val isSuccess: Success,
    override val final: Boolean = true
) : GameEvent(){
    val status = GameStatus.COMPLETED
}


@Serializable
data class GameDoesNotExistEvent(
    override val identifier: GameId,
    override val reason: Reason,
    override val final: Boolean = false
) : GameErrorEvent()

@Serializable
data class GameAlreadyExistsEvent(
    override val identifier: GameId,
    override val reason: Reason,
    override val final: Boolean = false
) : GameErrorEvent()

@Serializable
data class GameNotInCorrectState(
    override val identifier: GameId,
    override val reason: Reason,
    val status : GameStatus,
    override val final: Boolean = false
) : GameErrorEvent()
