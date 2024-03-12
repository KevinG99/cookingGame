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
    private val ingredientPreparationTimestampList = mutableListOf<IngredientPreparationTimestamp>()
    private val ingredientAddedTimestampList = mutableListOf<IngredientAddedTimestamp>()

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

    @Test
    fun `should add ingredient`() {
        val initializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val preparedEvent = IngredientPreparedEvent(ingredientId)
        val addedEvent = IngredientAddedEvent(ingredientId)
        val ingredientViewState = IngredientViewState(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime,
            IngredientStatus.ADDED,
            IngredientPreparationList(listOf(preparedEvent.preparationTimeStamp).toImmutableList()),
            IngredientAddedList(listOf(addedEvent.addedTimestamp).toImmutableList())
        )
        with(ingredientView) {
            givenEvents (
                listOf(initializedEvent, preparedEvent, addedEvent)
            ) thenState ingredientViewState
        }
    }
}
