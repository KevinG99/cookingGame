package com.cookingGame

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import com.cookingGame.plugins.configureSerialization
import com.fraktalio.adapter.persistence.AggregateEventRepositoryImpl
import com.fraktalio.adapter.persistence.MaterializedViewStateRepositoryImpl
import com.fraktalio.adapter.persistence.OrderRepository //TODO
import com.fraktalio.adapter.persistence.RestaurantRepository //TODO
import com.fraktalio.adapter.persistence.eventstore.EventStore
import com.fraktalio.adapter.persistence.eventstream.EventStreamProcessor
import com.fraktalio.adapter.persistence.extension.pooledConnectionFactory
import com.fraktalio.adapter.routes.restaurantRouting //TODO
import com.fraktalio.application.Aggregate
import com.fraktalio.application.aggregate
import com.fraktalio.application.materializedView
import com.fraktalio.application.paymentSagaManager
import com.fraktalio.domain.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.util.logging.*
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.awaitCancellation

/**
 * Simple logger
 */
internal val LOGGER = KtorSimpleLogger("com.fraktalio")

/**
 * Main entry point of the application
 * Arrow [SuspendApp] is used to gracefully handle termination of the application
 */
fun main(): Unit = SuspendApp {
    resourceScope {
        val httpEnv = Env.Http()
        val connectionFactory: ConnectionFactory = pooledConnectionFactory(Env.R2DBCDataSource())
        // ### Command Side - Event Sourcing ###
        val eventStore = EventStore(connectionFactory).apply { initSchema() }
        val aggregateEventRepository = AggregateEventRepositoryImpl(eventStore)
        val aggregate = aggregate(
            orderDecider(), //TODO
            restaurantDecider(), //TODO
            orderSaga(), //TODO
            restaurantSaga(), //TODO
            aggregateEventRepository
        )
        // ### Query Side - Event Streaming, Materialized Views and Sagas ###
        val eventStreamProcessor = EventStreamProcessor(connectionFactory).apply { initSchema() }
        val restaurantRepository = RestaurantRepository(connectionFactory).apply { initSchema() } //TODO
        val orderRepository = OrderRepository(connectionFactory).apply { initSchema() } //TODO
        val materializedViewStateRepository =
            MaterializedViewStateRepositoryImpl(restaurantRepository, orderRepository) //TODO //TODO

        @Suppress("UNUSED_VARIABLE")
        val materializedView = materializedView(
            restaurantView(), //TODO
            orderView(), //TODO
            materializedViewStateRepository
        ).also { eventStreamProcessor.registerMaterializedViewAndStartPooling("view", it, this@SuspendApp) }

        @Suppress("UNUSED_VARIABLE")
        val sagaManager = paymentSagaManager(
            paymentSaga(),
            aggregate
        ).also { eventStreamProcessor.registerSagaManagerAndStartPooling("saga", it, this@SuspendApp) }

        server(CIO, host = httpEnv.host, port = httpEnv.port) {
            configureSerialization()

            module(aggregate, restaurantRepository, orderRepository) //TODO //TODO
        }
        awaitCancellation()
    }
}

fun Application.module(
    aggregate: Aggregate,
    restaurantRepository: RestaurantRepository, //TODO
    orderRepository: OrderRepository //TODO
) {
    restaurantRouting(aggregate, restaurantRepository, orderRepository) //TODO //TODO
}
