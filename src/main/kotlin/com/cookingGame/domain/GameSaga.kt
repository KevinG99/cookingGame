package com.cookingGame.domain

import com.cookingGame.adapter.clients.GameClient
import com.cookingGame.adapter.persistence.GameRepository
import com.cookingGame.adapter.persistence.IngredientRepository
import com.fraktalio.fmodel.domain.Saga
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * A convenient type alias for Saga<GameEvent?, GameCommand>
 */
typealias GameSaga = Saga<Event?, Command>

/**
 * Saga is a datatype that represents the central point of control deciding what to execute next.
 * It is responsible for mapping different events from aggregates into action results (AR) that the [Saga] then can use to calculate the next actions (A) to be mapped to command of other aggregates.
 *
 * Saga does not maintain the state.
 *
 * `react` is a pure function/lambda that takes any event/action-result of type [GameEvent] as parameter, and returns the flow of commands/actions Flow<[GameCommand]> to be published further downstream.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun gameSaga(gameClient: GameClient, ingredientRepository: IngredientRepository, gameRepository: GameRepository) =
    GameSaga(
        react = { e ->
            when (e) {
                null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
                is GameCreatedEvent -> gameClient.getIngredients(e.name).flatMapConcat { ollamaResponse ->
                    flowOf(PrepareGameCommand(e.identifier, ollamaResponse.ingredientList, ollamaResponse.gameDuration))
                }

                is GamePreparedEvent -> flow {
                    e.ingredients.value.forEach { ingredient ->
                        val commandFlow = flowOf(
                            InitalizeIngredientCommand(
                                ingredient.id,
                                e.identifier,
                                ingredient.name,
                                ingredient.quantity,
                                ingredient.inputTime
                            )
                        )
                        emitAll(commandFlow)
                    }
                }

                is GameStartedEvent -> flowOf(StartGameTimerCommand(e.identifier))
                is GameTimeElapsedEvent -> emptyFlow()
                is GameEndedEvent -> flow {
                    val ingredientViewStates = ingredientRepository.findAllByGameId(e.identifier.value.toString())
                    emit(CalculateScoreCommand(e.identifier, ScoreCalculationInput(ingredientViewStates.toList())))
                }

                is GameCompletedEvent -> emptyFlow()
                is GameAlreadyExistsEvent -> emptyFlow()
                is GameDoesNotExistEvent -> emptyFlow()
                is GameNotInCorrectState -> emptyFlow()
                is IngredientAlreadyExistsEvent -> emptyFlow()
                is IngredientInitializedEvent -> flowOf(UpdateGameIngredientCommand(e.gameId, e.identifier, e.status))
                is GameIngredientUpdatedEvent -> emptyFlow()
                is GameDoesNotContainIngredientEvent -> emptyFlow()
                is IngredientDoesNotExistEvent -> emptyFlow()
                is IngredientNotInCorrectStateEvent -> emptyFlow()
                is IngredientPreparedEvent -> flow {
                    val ingredientViewState =
                        ingredientRepository.findById(e.identifier.value.toString()) ?: return@flow
                    if (ingredientViewState.preparationTimestamps.value.size >= ingredientViewState.quantity.value) {
                        emit(CompleteIngredientPreparationCommand(ingredientViewState.gameId, e.identifier))
                    }
                    return@flow
                }

                is IngredientPreparationCompletedEvent -> emptyFlow()
                is IngredientAddedEvent -> flow {
                    val ingredientViewState =
                        ingredientRepository.findById(e.identifier.value.toString()) ?: return@flow
                    //Todo: maybe check for how many ingredients have been added or somethign
                    emit(AddIngredientToGameCommand(ingredientViewState.gameId, e.identifier))
                    return@flow
                }

                is GameIngredientAdditionCompletedEvent -> flow {
                    val gameViewState = gameRepository.findById(e.identifier.value.toString()) ?: return@flow
                    if (gameViewState.ingredients?.value?.all { it.status == IngredientStatus.ADDED } == true) {
                        emit(EndGameCommand(e.identifier))
                    }
                }

                is ScoreCalculatedEvent -> emptyFlow()
            }
        }
    )
