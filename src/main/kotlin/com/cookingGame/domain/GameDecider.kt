package com.cookingGame.domain

import com.fraktalio.fmodel.domain.Decider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf


typealias GameDecider = Decider<GameCommand?, Game?, GameEvent?>


fun gameDecider() = GameDecider(
    initialState = null,
    decide = { c: GameCommand?, s: Game? ->
        when (c) {
            is GenerateGameCommand ->
                if (s == null) flowOf(GameGeneratedEvent(c.identifier, c.name, c.ingredients))
                else flowOf(GameNotCreatedEvent(c.identifier, c.name, Reason("Game already exists"), true))

            is StartGameCommand ->
                if (s == null) flowOf(GameStartedEvent(c.identifier, c.name, c.ingredients))
                else flowOf(GameNotCreatedEvent(c.identifier, c.name, Reason("Game already exists"), true))

            null -> emptyFlow()
        }
    },
    evolve = { s, e ->
        when (e) {
            is GameGeneratedEvent -> Game(e.identifier, e.name, e.ingredients)
            is GameStartedEvent -> s?.copy(name = e.name, ingredients = e.ingredients)
            is GameNotCreatedEvent -> s
            else -> s
        }

    }
)


data class Game(
    val id: GameId,
    val name: GameName,
    val ingredients: ImmutableList<IngredientItem>
)