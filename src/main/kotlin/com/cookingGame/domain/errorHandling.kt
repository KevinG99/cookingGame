package com.cookingGame.domain

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Reason(val value: String)


@Serializable
enum class Error(val reason: Reason) {
    GameAlreadyExists(Reason("Game already exists")),
    GameDoesNotExist(Reason("Game does not exist")),
    GameNotCreated(Reason("Game not in CREATED status")),
    GameNotPrepared(Reason("Game not in PREPARED status")),
    GameNotStarted(Reason("Game not in STARTED status"))
}