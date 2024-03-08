package com.cookingGame.application

import com.cookingGame.LOGGER
import com.cookingGame.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

class GameService {
    private val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun startGameTimer(gameId: GameId, startTime: GameStartTime, gameDuration: GameDuration): Flow<GameCommand> = flow {
        val endTime = startTime.value.plus(gameDuration.value.longValueExact(), DateTimeUnit.SECOND)
        LOGGER.info("End time: $endTime")

        val channel = Channel<GameCommand>()

        timerScope.launch {
            var clock = Clock.System.now()
            while (clock < endTime) {
                clock = Clock.System.now()
                if (clock > endTime) break
            }
            channel.send(CheckGameTimerCommand(gameId))
            LOGGER.info("Command emitted: $clock")
            channel.close()
        }

        channel.consume {
            emit(this.receive())
        }
    }
}
