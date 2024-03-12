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
}

@Serializable
data class GameEndedEvent(
    override val identifier: GameId,
    val score : GameScore,
    override val final: Boolean = false
) : GameEvent(){
    val status = GameStatus.GAME_ENDED
    val completionTime = GameCompletionTime()
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


// INGREDIENTS
@Serializable
sealed class IngredientEvent : Event() {
    abstract val identifier: IngredientId
}

@Serializable
sealed class IngredientErrorEvent : IngredientEvent() {
    abstract val reason: Reason
}

@Serializable
data class IngredientInitializedEvent(
    override val identifier: IngredientId,
    val gameId: GameId,
    val ingredientName: IngredientName,
    val ingredientQuantity: IngredientQuantity,
    val inputTime: IngredientInputTime,
    override val final: Boolean = false
) : IngredientEvent(){
    val status = IngredientStatus.INITIALIZED
}

@Serializable
data class IngredientAlreadyExistsEvent(
    override val identifier: IngredientId,
    override val reason: Reason,
    override val final: Boolean = false
) : IngredientErrorEvent()

@Serializable
data class GameDoesNotContainIngredientEvent(
    override val identifier: GameId,
    val ingredientId: IngredientId,
    override val reason: Reason,
    override val final: Boolean = false
) : GameErrorEvent()

@Serializable
data class GameIngredientUpdatedEvent(
    override val identifier: GameId,
    val ingredientId: IngredientId,
    val ingredientStatus: IngredientStatus,
    override val final: Boolean = false
) : GameEvent()

@Serializable
data class IngredientPreparedEvent(
    override val identifier: IngredientId,
    override val final: Boolean = false,
) : IngredientEvent(){
    val preparationTimeStamp = IngredientPreparationTimestamp()
    val status = IngredientStatus.PREPARED
}

@Serializable
data class IngredientDoesNotExistEvent(
    override val identifier: IngredientId,
    override val reason: Reason,
    override val final: Boolean = false
) : IngredientErrorEvent()

@Serializable
data class IngredientNotInCorrectStateEvent(
    override val identifier: IngredientId,
    override val reason: Reason,
    val status : IngredientStatus,
    override val final: Boolean = false
) : IngredientErrorEvent()

@Serializable
data class IngredientPreparationCompletedEvent(
    override val identifier: GameId,
    val ingredientId: IngredientId,
    override val final: Boolean = false
) : GameEvent(){
    val ingredientStatus = IngredientStatus.PREPARED
    val preparationCompleteTime : IngredientPreparationTimestamp = IngredientPreparationTimestamp()
}