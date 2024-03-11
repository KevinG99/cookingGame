package com.cookingGame.domain

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Reason(val value: String)


@Serializable
enum class Error(val reason: Reason) {
    GameAlreadyExists(Reason("Game already exists")),
    GameDoesNotExist(Reason("Game does not exist")),
    GameNotInCorrectState(Reason("Game not in correct state")),
    IngredientAlreadyExists(Reason("Ingredient already exists")),
}