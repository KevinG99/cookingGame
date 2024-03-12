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
    private val ingredientPreparationTimestampList = mutableListOf<IngredientPreparationTimestamp>()
    private val ingredientAddedTimestampList = mutableListOf<IngredientAddedTimestamp>()


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

    //is AddIngredientCommand -> if (ingredient == null) flowOf(
    //                IngredientDoesNotExistEvent(
    //                    ingredientCommand.identifier,
    //                    Error.IngredientDoesNotExist.reason
    //                )
    //            )
    //            else if (IngredientStatus.PREPARED != ingredient.status) flowOf(
    //                IngredientNotInCorrectStateEvent(
    //                    ingredientCommand.identifier,
    //                    Error.IngredientNotInCorrectState.reason,
    //                    ingredient.status
    //                )
    //            )
    //            else flowOf(
    //                IngredientAddedEvent(
    //                    ingredientCommand.identifier,
    //                )
    //            )
    @Test
    fun `should add ingredient`() = runTest {
        val addIngredientCommand = AddIngredientCommand(
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
        val ingredientAddedEvent = IngredientAddedEvent(ingredientId)
        with(ingredientDecider) {
            givenEvents(listOf(ingredientInitializedEvent, ingredientPreparedEvent)) {
                whenCommand(addIngredientCommand)
            } thenEvents listOf(ingredientAddedEvent)
        }
    }

    @Test
    fun `should not add ingredient if not in correct state`() = runTest {
        val addIngredientCommand = AddIngredientCommand(
            ingredientId
        )
        val ingredietInitializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientNotInCorrectStateEvent = IngredientNotInCorrectStateEvent(
            ingredientId,
            Error.IngredientNotInCorrectState.reason,
            IngredientStatus.INITIALIZED
        )
        with(ingredientDecider) {
            givenEvents(listOf(ingredietInitializedEvent)) {
                whenCommand(addIngredientCommand)
            } thenEvents listOf(ingredientNotInCorrectStateEvent)
        }
    }

    @Test
    fun `should not add ingredient if does not exist`() = runTest {
        val addIngredientCommand = AddIngredientCommand(
            ingredientId
        )
        val ingredientDoesNotExistEvent = IngredientDoesNotExistEvent(
            ingredientId,
            Error.IngredientDoesNotExist.reason
        )
        with(ingredientDecider) {
            givenEvents(emptyList()) {
                whenCommand(addIngredientCommand)
            } thenEvents listOf(ingredientDoesNotExistEvent)
        }
    }

}