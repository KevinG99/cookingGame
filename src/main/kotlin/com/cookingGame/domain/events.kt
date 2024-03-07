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
    val name: GameName,
    val ingredients: IngredientList,
    override val final: Boolean = false
) : GameEvent(){
    val status = GameStatus.PREPARED
}


@Serializable
data class GameStartedEvent(
    override val identifier: GameId,
    val name: GameName,
    val ingredients : IngredientList,
    val startTime: GameStartTime,
    override val final: Boolean = false
) : GameEvent(){
    val status = GameStatus.STARTED
}

@Serializable
data class GameDoesNotExistEvent(
    override val identifier: GameId,
    val name: GameName,
    override val reason: Reason,
    override val final: Boolean = false
) : GameErrorEvent()

@Serializable
data class GameAlreadyExistsEvent(
    override val identifier: GameId,
    val name: GameName,
    override val reason: Reason,
    override val final: Boolean = false
) : GameErrorEvent()


@Serializable
data class GameNotInCreatableStateEvent(
    override val identifier: GameId,
    val name: GameName,
    override val reason: Reason,
    override val final: Boolean = false
) : GameErrorEvent()

@Serializable
data class GameNotInPreparedStateEvent(
    override val identifier: GameId,
    val name: GameName,
    override val reason: Reason,
    override val final: Boolean = false
) : GameErrorEvent()