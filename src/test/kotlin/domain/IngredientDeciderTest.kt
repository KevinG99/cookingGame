package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import thenEvents
import whenCommand
import java.math.BigDecimal


class IngredientDeciderTest {

    private val ingredientDecider = ingredientDecider()
    private val gameId = GameId()
    private val ingredientId = IngredientId()
    private val ingredientName = IngredientName("ingredientName")
    private val quantity = IngredientQuantity(10)
    private val inputTime = IngredientInputTime(BigDecimal.TEN)


    @Test
    fun `should create ingredient`() = runTest {
        val initalizeIngredientCommand = InitalizeIngredientCommand(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientInitializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        with(ingredientDecider) {
            givenEvents(emptyList()) {
                whenCommand(initalizeIngredientCommand)
            } thenEvents listOf(ingredientInitializedEvent)
        }
    }

    @Test
    fun `should not create ingredient if already exists`() = runTest {
        val initalizeIngredientCommand = InitalizeIngredientCommand(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientInitializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientAlreadyExistsEvent = IngredientAlreadyExistsEvent(
            ingredientId,
            Error.IngredientAlreadyExists.reason,
            true
        )
        with(ingredientDecider) {
            givenEvents(listOf(ingredientInitializedEvent)) {
                whenCommand(initalizeIngredientCommand)
            } thenEvents listOf(ingredientAlreadyExistsEvent)
        }
    }

    @Test
    fun `should prepare ingredient`() = runTest {
        val prepareIngredientCommand = PrepareIngredientCommand(
            ingredientId
        )
        val ingredientInitializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientPreparedEvent = IngredientPreparedEvent(ingredientId)
        with(ingredientDecider) {
            givenEvents(listOf(ingredientInitializedEvent)) {
                whenCommand(prepareIngredientCommand)
            } thenEvents listOf(ingredientPreparedEvent)
        }
    }

    @Test
    fun `should not prepare ingredient if not in correct state`() = runTest {
        val prepareIngredientCommand = PrepareIngredientCommand(
            ingredientId
        )
        val initializedIngredient = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientPreparedEvent = IngredientPreparedEvent(ingredientId)
        val ingredientNotInCorrectStateEvent = IngredientNotInCorrectStateEvent(
            ingredientId,
            Error.IngredientNotInCorrectState.reason,
            IngredientStatus.PREPARED
        )
        with(ingredientDecider) {
            givenEvents(listOf(initializedIngredient, ingredientPreparedEvent)) {
                whenCommand(prepareIngredientCommand)
            } thenEvents listOf(ingredientNotInCorrectStateEvent)
        }
    }

    @Test
    fun `should not prepare ingredient if does not exist`() = runTest {
        val prepareIngredientCommand = PrepareIngredientCommand(
            ingredientId
        )
        with(ingredientDecider) {
            givenEvents(emptyList()) {
                whenCommand(prepareIngredientCommand)
            } thenEvents listOf(IngredientDoesNotExistEvent(ingredientId, Error.IngredientDoesNotExist.reason))
        }
    }
}