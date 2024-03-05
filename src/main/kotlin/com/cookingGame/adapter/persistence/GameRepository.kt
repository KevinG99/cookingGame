package com.cookingGame.adapter.persistence

import com.cookingGame.LOGGER
import com.cookingGame.adapter.persistence.extension.alterSQLResource
import com.cookingGame.adapter.persistence.extension.connection
import com.cookingGame.domain.GameViewState
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

class GameRepository(private val connectionFactory: ConnectionFactory) {
    companion object {
        private const val CREATE_ORDER =
            """
              CREATE TABLE IF NOT EXISTS games (
                game_id VARCHAR PRIMARY KEY,
                game_data JSONB
                );
            """
        private const val CREATE_OR_UPDATE_ORDER_FUN =
            """
                -- Function to insert or update a order entity and return the updated/created row
                CREATE OR REPLACE FUNCTION insert_update_order(
                    p_order_id VARCHAR, p_order_data JSONB
                )
                RETURNS SETOF "orders" AS $$
                BEGIN
                    -- Update or insert the order table
                    RETURN QUERY
                        INSERT INTO orders (order_id, order_data)
                        VALUES (p_order_id, p_order_data)
                        ON CONFLICT (order_id) DO UPDATE
                        SET order_data = EXCLUDED.order_data
                        RETURNING *;
                END;
                $$ LANGUAGE plpgsql;
            """
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dbDispatcher = Dispatchers.IO.limitedParallelism(10)
    suspend fun initSchema() = withContext(dbDispatcher) {
        LOGGER.debug("# Initializing Order schema #")
        connectionFactory.connection().alterSQLResource(CREATE_ORDER)
        connectionFactory.connection().alterSQLResource(CREATE_OR_UPDATE_ORDER_FUN)

    }

    suspend fun upsertOrder(order: OrderViewState?) = withContext(dbDispatcher) {
        if (order != null)
            connectionFactory.connection()
                .executeSql(
                    """
                SELECT * FROM insert_update_order($1, $2)
                """,
                    orderMapper
                ) {
                    bind(0, order.id.value.toString())
                    bind(
                        1,
                        JsonPostgres.of(
                            Json.encodeToString(order).encodeToByteArray()
                        )
                    )
                }
                .singleOrNull()
        else null
    }

    suspend fun findById(id: String) = withContext(dbDispatcher) {
        connectionFactory.connection()
            .executeSql(
                """
                SELECT * FROM orders WHERE order_id = $1
                """,
                orderMapper
            ) {
                bind(0, id)
            }
            .singleOrNull()
    }

    fun findAll() = flow {
        connectionFactory.connection()
            .executeSql(
                """
                SELECT * FROM orders
                """,
                orderMapper
            )
            .also { emitAll(it) }
    }.flowOn(dbDispatcher)

    fun upsertGame(game: GameViewState?): GameViewState? {
        TODO("Not yet implemented")
    }
}