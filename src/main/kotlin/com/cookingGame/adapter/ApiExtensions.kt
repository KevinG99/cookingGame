package com.cookingGame.adapter

import com.cookingGame.domain.*

fun Command.deciderId() = when (this) {
    is IngredientCommand -> identifier.value.toString()
    is GameCommand -> identifier.value.toString()
}

fun Event.deciderId() = when (this) {
    is IngredientEvent -> identifier.value.toString()
    is GameEvent -> identifier.value.toString()
}

fun Event.decider() = when (this) {
    is IngredientEvent -> "Ingredient"
    is GameEvent -> "Game"
}

fun Event.event() = this.javaClass.name


