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
    GameDoesNotHaveIngredient(Reason("Game does not have ingredient")),
    IngredientAlreadyExists(Reason("Ingredient already exists")),
    IngredientDoesNotExist(Reason("Ingredient does not exist")),
    IngredientNotInCorrectState(Reason("Ingredient not in correct state")),
}