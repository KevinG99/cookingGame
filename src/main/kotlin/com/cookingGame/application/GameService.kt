package com.cookingGame.application

import com.cookingGame.LOGGER
import com.cookingGame.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

class GameService {
    private val job = Job()
    private val coroutineScope = CoroutineScope(job + Dispatchers.Default)

    fun startGameTimer(gameId: GameId, startTime: GameStartTime, gameDuration: GameDuration): Flow<GameCommand> = flow {
        val endTime = startTime.value.plus(gameDuration.value.longValueExact(), DateTimeUnit.SECOND)
        LOGGER.info("End time: $endTime")
        var clock = Clock.System.now()
        while (clock < endTime) {
            clock = Clock.System.now()
            if (clock > endTime) break
        }
        emit(CheckGameTimerCommand(gameId))
        LOGGER.info("Command emitted: $clock")
    }

    fun stopGame(gameId: GameId) {
        job.cancel() // Stops the coroutine when the game stops
    }
}
