package com.cookingGame

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import com.cookingGame.adapter.clients.GameClient
import com.cookingGame.adapter.persistence.AggregateEventRepositoryImpl
import com.cookingGame.adapter.persistence.GameRepository
import com.cookingGame.adapter.persistence.IngredientRepository
import com.cookingGame.adapter.persistence.MaterializedViewStateRepositoryImpl
import com.cookingGame.adapter.persistence.eventstore.EventStore
import com.cookingGame.adapter.persistence.eventstream.EventStreamProcessor
import com.cookingGame.adapter.persistence.extension.pooledConnectionFactory
import com.cookingGame.adapter.routes.cookingGameRouting
import com.cookingGame.application.Aggregate
import com.cookingGame.application.aggregate
import com.cookingGame.application.materializedView
import com.cookingGame.domain.*
import com.cookingGame.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.util.logging.*
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.awaitCancellation

/**
 * Simple logger
 */
internal val LOGGER = KtorSimpleLogger("com.cookingGame")

/**
 * Main entry point of the application
 * Arrow [SuspendApp] is used to gracefully handle termination of the application
 */
fun main(): Unit = SuspendApp {
    resourceScope {
        val httpEnv = Env.Http()
        val connectionFactory: ConnectionFactory = pooledConnectionFactory(Env.R2DBCDataSource())
        // ### Query Side - Event Streaming, Materialized Views and Sagas ###
        val eventStreamProcessor = EventStreamProcessor(connectionFactory).apply { initSchema() }
        val ingredientRepository = IngredientRepository(connectionFactory).apply { initSchema() }
        val gameRepository = GameRepository(connectionFactory).apply { initSchema() }
        val materializedViewStateRepository =
            MaterializedViewStateRepositoryImpl(gameRepository, ingredientRepository)

        // ### Command Side - Event Sourcing ###
        val gameClient = GameClient()
        val eventStore = EventStore(connectionFactory).apply { initSchema() }
        val aggregateEventRepository = AggregateEventRepositoryImpl(eventStore)
        val aggregate = aggregate(
            gameDecider(),
            ingredientDecider(),
            gameSaga(gameClient, ingredientRepository, gameRepository),
            aggregateEventRepository
        )

        @Suppress("UNUSED_VARIABLE")
        val materializedView = materializedView(
            gameView(),
            ingredientView(),
            materializedViewStateRepository
        ).also { eventStreamProcessor.registerMaterializedViewAndStartPooling("view", it, this@SuspendApp) }

        server(CIO, host = httpEnv.host, port = httpEnv.port) {
            configureSerialization()

            module(aggregate, ingredientRepository, gameRepository)
        }
        awaitCancellation()
    }
}

fun Application.module(
    aggregate: Aggregate,
    ingredientRepository: IngredientRepository,
    gameRepository: GameRepository
) {
    cookingGameRouting(aggregate, ingredientRepository, gameRepository)
}
