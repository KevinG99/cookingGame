package com.cookingGame.adapter.routes

import com.cookingGame.LOGGER
import com.cookingGame.adapter.persistence.GameRepository
import com.cookingGame.adapter.persistence.IngredientRepository
import com.cookingGame.application.Aggregate
import com.cookingGame.domain.Command
import com.cookingGame.domain.Event
import com.fraktalio.fmodel.application.handleOptimistically
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

@OptIn(ExperimentalCoroutinesApi::class)
fun Application.cookingGameRouting(
    aggregate: Aggregate,
    ingredientRepository: IngredientRepository,
    gameRepository: GameRepository,
) {

    routing {
        post("/commands") {
            try {
                val command = call.receive<Command>()
                val resultEvents: List<Event> =
                    aggregate.handleOptimistically(command).map { it.first }.filterNotNull().toList()

                call.respond(HttpStatusCode.Created, resultEvents)
            } catch (e: Exception) {
                LOGGER.error("Error: ${e.message}", e)
                call.respond(HttpStatusCode.BadRequest)
            }        }
        get("/ingredients") {
            // TODO
        }
        get("ingredient/{id}") {
            try {
                val ingredient = ingredientRepository.findById(call.parameters["id"] ?: throw IllegalArgumentException("Id is missing"))
                if (ingredient != null) {
                    call.respond(ingredient)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: Exception) {
                LOGGER.error("Error: ${e.message}", e)
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/games") {
            try {
                val games = gameRepository.findAll().toList()
                call.respond(games)
            } catch (e: Exception) {
                LOGGER.error("Error: ${e.message}", e)
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        get("/games/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Id is missing")
                val game = gameRepository.findById(id)
                if (game != null) {
                    call.respond(game)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: Exception) {
                LOGGER.error("Error: ${e.message}", e)
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
