package com.cookingGame.adapter.persistence

import com.cookingGame.LOGGER
import com.cookingGame.adapter.persistence.extension.alterSQLResource
import com.cookingGame.adapter.persistence.extension.connection
import com.cookingGame.adapter.persistence.extension.executeSql
import com.cookingGame.domain.GameViewState
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.r2dbc.postgresql.codec.Json as JsonPostgres

class GameRepository(private val connectionFactory: ConnectionFactory) {
    companion object {
        private const val CREATE_GAME =
            """
              CREATE TABLE IF NOT EXISTS games (
                game_id VARCHAR PRIMARY KEY,
                game_data JSONB
                );
            """
        private const val CREATE_OR_UPDATE_GAME_FUN =
            """
                -- Function to insert or update a game entity and return the updated/created row
                CREATE OR REPLACE FUNCTION insert_update_game(
                    p_game_id VARCHAR, p_game_data JSONB
                )
                RETURNS SETOF "games" AS $$
                BEGIN
                    -- Update or insert the game table
                    RETURN QUERY
                        INSERT INTO games (game_id, game_data)
                        VALUES (p_game_id, p_game_data)
                        ON CONFLICT (game_id) DO UPDATE
                        SET game_data = EXCLUDED.game_data
                        RETURNING *;
                END;
                $$ LANGUAGE plpgsql;

            """
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dbDispatcher = Dispatchers.IO.limitedParallelism(10)
    suspend fun initSchema() = withContext(dbDispatcher) {
        LOGGER.debug("# Initializing Game schema #")
        connectionFactory.connection().alterSQLResource(CREATE_GAME)
        connectionFactory.connection().alterSQLResource(CREATE_OR_UPDATE_GAME_FUN)

    }


    suspend fun upsertGame(game: GameViewState?) = withContext(dbDispatcher) {
        if (game != null)
            connectionFactory.connection()
                .executeSql(
                    """
                SELECT * FROM insert_update_game($1, $2)
                """,
                    gameMapper
                ) {
                    bind(0, game.id.value.toString())
                    bind(
                        1,
                        JsonPostgres.of(
                            Json.encodeToString(game).encodeToByteArray()
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
                SELECT * FROM games WHERE game_id = $1
                """,
                gameMapper
            ) {
                bind(0, id)
            }
            .singleOrNull()
    }

    fun findAll() = flow {
        connectionFactory.connection()
            .executeSql(
                """
                SELECT * FROM games
                """,
                gameMapper
            )
            .also { emitAll(it) }
    }.flowOn(dbDispatcher)
}

private val gameMapper: (Row, RowMetadata) -> GameViewState = { row, _ ->
    Json.decodeFromString<GameViewState>(row.get("game_data", ByteArray::class.java)!!.decodeToString())
}