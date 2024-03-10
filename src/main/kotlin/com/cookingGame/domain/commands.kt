package com.cookingGame.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class Command

@Serializable
sealed class GameCommand : Command() {
    abstract val identifier: GameId
}

@Serializable
sealed class IngredientCommand : Command() {
    abstract val identifier: IngredientId
}


@Serializable
data class CreateGameCommand(
    override val identifier: GameId,
    val name: GameName,
) : GameCommand()

@Serializable
data class PrepareGameCommand(
    override val identifier: GameId,
    val ingredients: IngredientList,
    val gameDuration: GameDuration
) : GameCommand()

@Serializable
data class StartGameCommand(
    override val identifier: GameId,
    val ingredients: IngredientList,
    val gameDuration: GameDuration,
) : GameCommand()


@Serializable
data class StartGameTimerCommand(
    override val identifier: GameId
) : GameCommand()

@Serializable
data class EndGameCommand(
    override val identifier: GameId,
    val score : GameScore,
    val completionTime: GameCompletionTime,
) : GameCommand()

@Serializable
data class CompleteGameCommand(
    override val identifier: GameId,
) : GameCommand()