package com.cookingGame.application

import com.cookingGame.LOGGER
import com.cookingGame.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

class GameService {
    private val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val activeTimers = mutableMapOf<GameId, CompletableJob>()

    fun startGameTimer(gameId: GameId, startTime: GameStartTime, gameDuration: GameDuration): Flow<GameCommand> = flow {
        val endTime = startTime.value.plus(gameDuration.value.longValueExact(), DateTimeUnit.SECOND)

        val channel = Channel<GameCommand>()
        val job = Job(timerScope.coroutineContext[Job])

        activeTimers[gameId] = job

        timerScope.launch(job) {
            var clock = Clock.System.now()
            try {
                while (isActive && clock < endTime) {
                    clock = Clock.System.now()
                    if (clock > endTime) break
                }
                channel.send(StartGameTimerCommand(gameId))
                LOGGER.info("Command emitted: $clock")
                channel.close()
            } catch (e: CancellationException) {
                LOGGER.error("Timer canceled: $gameId", e)
                channel.close(e)
            } finally {
                activeTimers.remove(gameId)
            }
        }

        channel.consume {
            emit(receive())
        }
    }.onCompletion { activeTimers.remove(gameId) }

    fun stopGameTimer(gameId: GameId) {
        activeTimers[gameId]?.cancel(CancellationException("Game timer stopped"))
    }
}