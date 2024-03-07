package com.cookingGame.adapter.clients

import com.cookingGame.Env
import com.cookingGame.LOGGER
import com.cookingGame.domain.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import java.math.BigDecimal


class GameClient(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }
) {
    private val ollama = Env.Ollama()
    private val ollamaRoute = "http://${ollama.host}:${ollama.port}"

    fun getIngredients(name: GameName): Flow<IngredientList> = flow {
        try {
            val response: HttpResponse = client.get("$ollamaRoute/ingredients?gameName=${name.value}")
            if (response.status == HttpStatusCode.OK) {
                val ingredients = response.body<List<IngredientItem>>() //TODO: check if this works
                emit(IngredientList(ingredients.toImmutableList()))
            } else {
                LOGGER.error("Received non-OK response: ${response.status}")
                emit(
                    IngredientList(
                        listOf(
                            IngredientItem(
                                IngredientId(),
                                IngredientName("Test ingredient 1"),
                                IngredientQuantity(5),
                                IngredientInputTime(BigDecimal.TEN)
                            ),
                            IngredientItem(
                                IngredientId(),
                                IngredientName("Test ingredient 2"),
                                IngredientQuantity(5),
                                IngredientInputTime(BigDecimal.valueOf(20))
                            ),
                            IngredientItem(
                                IngredientId(),
                                IngredientName("Test ingredient 3"),
                                IngredientQuantity(5),
                                IngredientInputTime(BigDecimal.TEN)
                            )
                        ).toImmutableList()
                    )
                )
            }
        } catch (e: Exception) {
            LOGGER.error("Error fetching ingredients for $name, returning hardcoded list. Error: ${e.message}")
        }
    }.flowOn(dispatcher)
}