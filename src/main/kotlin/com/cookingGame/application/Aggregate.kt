package com.cookingGame.application

import com.cookingGame.domain.*
import com.fraktalio.fmodel.application.EventLockingRepository
import com.fraktalio.fmodel.application.EventSourcingLockingOrchestratingAggregate
import com.fraktalio.fmodel.domain.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

internal typealias AggregateEventRepository = EventLockingRepository<Command?, Event?, UUID?>
internal typealias Aggregate = EventSourcingLockingOrchestratingAggregate<Command?, Pair<Game?, Ingredient?>, Event?, UUID?>

@OptIn(ExperimentalCoroutinesApi::class)
internal fun aggregate(
    gameDecider: GameDecider,
    ingredientDecider: IngredientDecider,
    gameSaga: GameSaga,
    ingredientSaga: IngredientSaga,
    eventRepository: AggregateEventRepository,
): Aggregate = EventSourcingLockingOrchestratingAggregate(
    // Combining two deciders into one.
    decider = gameDecider.combine(ingredientDecider),
    // How and where do you want to store new events.
    eventRepository = eventRepository,
    // Combining individual choreography Sagas into one orchestrating Saga.
    saga = gameSaga.combine(ingredientSaga)
)
