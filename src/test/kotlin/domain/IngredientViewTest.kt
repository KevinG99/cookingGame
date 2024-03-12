package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.collections.immutable.toImmutableList
import org.junit.jupiter.api.Test
import thenState
import java.math.BigDecimal

class IngredientViewTest {
    private val ingredientView = ingredientView()
    private val gameId = GameId()
    private val ingredientId = IngredientId()
    private val ingredientName = IngredientName("ingredientName")
    private val quantity = IngredientQuantity(10)
    private val inputTime = IngredientInputTime(BigDecimal.TEN)
    private val emptyIngredientPreparationList = IngredientPreparationList()
    private val ingredientPreparationTimestamp = IngredientPreparationTimestamp()

    @Test
    fun `should initialize ingredient`() {
        val initializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientViewState = IngredientViewState(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime,
            IngredientStatus.INITIALIZED
        )
        with(ingredientView) {
            givenEvents (
                listOf(initializedEvent)
            ) thenState ingredientViewState
        }
    }

    @Test
    fun `should prepare ingredient`() {
        val initializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val preparedEvent = IngredientPreparedEvent(ingredientId)
        val ingredientViewState = IngredientViewState(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime,
            IngredientStatus.PREPARED,
            IngredientPreparationList(listOf(preparedEvent.preparationTimeStamp).toImmutableList())
        )
        with(ingredientView) {
            givenEvents (
                listOf(initializedEvent, preparedEvent)
            ) thenState ingredientViewState
        }
    }
}
