package com.cookingGame.adapter.routes

import com.cookingGame.adapter.persistence.GameRepository
import com.cookingGame.adapter.persistence.IngredientRepository
import com.cookingGame.application.Aggregate
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun Application.cookingGameRouting(
    aggregate: Aggregate,
    ingredientRepository: IngredientRepository,
    gameRepository: GameRepository,
) {

    routing {
    }
}
