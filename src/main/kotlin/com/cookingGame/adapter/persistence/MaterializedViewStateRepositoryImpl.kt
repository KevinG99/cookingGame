package com.cookingGame.adapter.persistence

import com.cookingGame.LOGGER
import com.cookingGame.adapter.deciderId
import com.cookingGame.application.MaterializedViewState
import com.cookingGame.application.MaterializedViewStateRepository
import com.cookingGame.domain.Event

/**
 * View repository implementation
 *
 * @constructor Create Materialized View repository impl
 *
 */

internal open class MaterializedViewStateRepositoryImpl(
    private val gameRepository: GameRepository,
    private val ingredientRepository: IngredientRepository
) :
    MaterializedViewStateRepository {

    override suspend fun Event?.fetchState(): MaterializedViewState {
        LOGGER.debug("view / event-handler: fetchState({}) started ...", this)
        return MaterializedViewState(
            this?.let { gameRepository.findById(it.deciderId()) },
            this?.let { ingredientRepository.findById(it.deciderId()) }
        )

    }

    override suspend fun MaterializedViewState.save(): MaterializedViewState =
        with(this) {
            LOGGER.debug("view / event-handler: save({}) started ... #########", this)
            MaterializedViewState(
                gameRepository.upsertGame(game),
                ingredientRepository.upsertIngredient(ingredient)
            )
        }
}
