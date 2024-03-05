package com.cookingGame.application

import com.cookingGame.domain.*
import com.fraktalio.fmodel.application.MaterializedView
import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.domain.combine

/**
 * A convenient type alias for ViewStateRepository<Event?, MaterializedViewState>
 */
typealias MaterializedViewStateRepository = ViewStateRepository<Event?, MaterializedViewState>

/**
 * A convenient type alias for MaterializedView<MaterializedViewState, Event?>
 */
typealias OrderRestaurantMaterializedView = MaterializedView<MaterializedViewState, Event?>

/**
 * One, big materialized view that is `combining` all views: [ingredientView], [gameView].
 * Every event will be handled by one of the views.
 * The view that is not interested in specific event type will simply ignore it (do nothing).
 *
 * @param gameView gameView is used internally to handle events and maintain a view state.
 * @param ingredientView ingredientView is used internally to handle events and maintain a view state.
 * @param viewStateRepository is used to store the newly produced view state of the Restaurant and/or Restaurant order together
 *
 */
internal fun materializedView(
    gameView: GameView,
    ingredientView: IngredientView,
    viewStateRepository: MaterializedViewStateRepository
): OrderRestaurantMaterializedView = MaterializedView(
    // Combining two views into one, and (di)map the inconvenient Pair into a domain specific Data class (MaterializedViewState) that will represent view state better.
    view = gameView.combine(ingredientView).dimapOnState(
        fl = { Pair(it.game, it.ingredient) },
        fr = { MaterializedViewState(it.first, it.second) }
    ),
    viewStateRepository = viewStateRepository
)

/**
 * A domain specific representation of the combined view state.
 */
data class MaterializedViewState(val game: GameViewState?, val ingredient: IngredientViewState?)
