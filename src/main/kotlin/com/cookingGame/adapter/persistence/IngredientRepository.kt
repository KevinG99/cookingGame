package com.cookingGame.adapter.persistence

import com.cookingGame.LOGGER
import com.cookingGame.adapter.persistence.extension.alterSQLResource
import com.cookingGame.adapter.persistence.extension.connection
import com.cookingGame.adapter.persistence.extension.executeSql
import com.cookingGame.domain.IngredientViewState
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.r2dbc.postgresql.codec.Json as JsonPostgres

class IngredientRepository(private val connectionFactory: ConnectionFactory) {
    companion object {
        private const val CREATE_INGREDIENT =
            """
              CREATE TABLE IF NOT EXISTS ingredients (
                ingredient_id VARCHAR PRIMARY KEY,
                ingredient_data JSONB
                );
            """
        private const val CREATE_OR_UPDATE_INGREDIENT_FUN =
            """
                -- Function to insert or update a ingredient entity and return the updated/created row
                CREATE OR REPLACE FUNCTION insert_update_ingredient(
                    p_ingredient_id VARCHAR, p_ingredient_data JSONB
                )
                RETURNS SETOF "ingredients" AS $$
                BEGIN
                    -- Update or insert the ingredient table
                    RETURN QUERY
                        INSERT INTO ingredients (ingredient_id, ingredient_data)
                        VALUES (p_ingredient_id, p_ingredient_data)
                        ON CONFLICT (ingredient_id) DO UPDATE
                        SET ingredient_data = EXCLUDED.ingredient_data
                        RETURNING *;
                END;
                $$ LANGUAGE plpgsql;

            """
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dbDispatcher = Dispatchers.IO.limitedParallelism(10)
    suspend fun initSchema() = withContext(dbDispatcher) {
        LOGGER.debug("# Initializing Ingredient schema #")
        connectionFactory.connection().alterSQLResource(CREATE_INGREDIENT)
        connectionFactory.connection().alterSQLResource(CREATE_OR_UPDATE_INGREDIENT_FUN)

    }


    suspend fun upsertIngredient(ingredient: IngredientViewState?) = withContext(dbDispatcher) {
        if (ingredient != null)
            connectionFactory.connection()
                .executeSql(
                    """
                SELECT * FROM insert_update_ingredient($1, $2)
                """,
                    ingredientMapper
                ) {
                    bind(0, ingredient.id.value.toString())
                    bind(
                        1,
                        JsonPostgres.of(
                            Json.encodeToString(ingredient).encodeToByteArray()
                        )
                    )
                }
                .singleOrNull()
        else null
    }

    suspend fun findAllByGameId(gameId: String) = withContext(dbDispatcher) {
        connectionFactory.connection()
            .executeSql(
                """
                SELECT * FROM ingredients WHERE ingredient_data ->> 'gameId' = $1
                """,
                ingredientMapper
            ) {
                bind(0, gameId)
            }
            .toList()
    }

    suspend fun findById(id: String) = withContext(dbDispatcher) {
        connectionFactory.connection()
            .executeSql(
                """
                SELECT * FROM ingredients WHERE ingredient_id = $1
                """,
                ingredientMapper
            ) {
                bind(0, id)
            }
            .singleOrNull()
    }

    fun findAll() = flow {
        connectionFactory.connection()
            .executeSql(
                """
                SELECT * FROM ingredients
                """,
                ingredientMapper
            )
            .also { emitAll(it) }
    }.flowOn(dbDispatcher)
}

private val ingredientMapper: (Row, RowMetadata) -> IngredientViewState = { row, _ ->
    Json.decodeFromString<IngredientViewState>(row.get("ingredient_data", ByteArray::class.java)!!.decodeToString())
}