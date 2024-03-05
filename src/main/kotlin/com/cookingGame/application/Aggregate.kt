package com.cookingGame.application

import com.cookingGame.domain.Command
import com.cookingGame.domain.Event
import com.cookingGame.domain.Game
import com.fraktalio.fmodel.application.EventLockingRepository
import com.fraktalio.fmodel.application.EventSourcingLockingOrchestratingAggregate
import java.util.*

internal typealias AggregateEventRepository = EventLockingRepository<Command?, Event?, UUID?>
internal typealias Aggregate = EventSourcingLockingOrchestratingAggregate<Command?, Pair<Game?, Ingredient?>, Event?, UUID?>
