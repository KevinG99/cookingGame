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
    val name: GameName,
    val ingredients: IngredientList,
    val gameDuration: GameDuration
) : GameCommand()

@Serializable
data class StartGameCommand(
    override val identifier: GameId,
    val name: GameName,
    val ingredients: IngredientList,
    val startTime: GameStartTime,
    val gameDuration: GameDuration,
) : GameCommand()


@Serializable
data class StartTimerCommand(
    override val identifier: GameId,
    val startTime: GameStartTime
) : GameCommand()

