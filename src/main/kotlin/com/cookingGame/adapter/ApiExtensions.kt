package com.cookingGame.adapter

import com.cookingGame.domain.*

fun Command.deciderId() = when (this) {
    is GameCommand -> identifier.value.toString()
}

fun Event.deciderId() = when (this) {
    is GameEvent -> identifier.value.toString()
}

fun Event.decider() = when (this) {
    is GameEvent -> "Game"
}

fun Event.event() = this.javaClass.name


