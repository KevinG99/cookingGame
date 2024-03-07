package domain

import com.cookingGame.domain.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SerializerTest {
    private val gameId = GameId()
    private val gameName = GameName("gameName")

    @Test
    fun serializeEventsWithKotlinSerializer1Test() {
        val expectedGameCreatedEventString =
            "{\"type\":\"com.cookingGame.domain.GameCreatedEvent\",\"identifier\":\"${gameId.value}\",\"name\":\"gameName\"}"
        val gameCreatedEvent: Event = GameCreatedEvent(
            gameId,
            gameName
        )
        assertEquals(expectedGameCreatedEventString, Json.encodeToString(gameCreatedEvent))
        assertEquals(gameCreatedEvent, Json.decodeFromString<Event>(expectedGameCreatedEventString))
    }

    @Test
    fun serializeEventsWithKotlinSerializer2Test() {
        val expectedGameAlreadyExistsEventString =
            "{\"type\":\"com.cookingGame.domain.GameAlreadyExistsEvent\",\"identifier\":\"${gameId.value}\",\"name\":\"gameName\",\"reason\":\"test\",\"final\":true}"
        val gameAlreadyExistsEvent: Event = GameAlreadyExistsEvent(
            gameId,
            gameName,
            Reason("test"),
            true
        )
        assertEquals(expectedGameAlreadyExistsEventString, Json.encodeToString(gameAlreadyExistsEvent))
        assertEquals(gameAlreadyExistsEvent, Json.decodeFromString<Event>(expectedGameAlreadyExistsEventString))
    }
}