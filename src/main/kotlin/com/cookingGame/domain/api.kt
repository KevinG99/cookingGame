package com.cookingGame.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.util.*

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): BigDecimal = decoder.decodeString().toBigDecimal()
    override fun serialize(encoder: Encoder, value: BigDecimal) = encoder.encodeString(value.toPlainString())
}

class ImmutableListSerializer<T>(elementSerializer: KSerializer<T>) : KSerializer<ImmutableList<T>> {
    private val delegateSerializer = ListSerializer(elementSerializer)

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("ImmutableList", elementSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: ImmutableList<T>) =
        encoder.encodeSerializableValue(delegateSerializer, value)

    override fun deserialize(decoder: Decoder) = decoder.decodeSerializableValue(delegateSerializer)
        .toImmutableList()
}

@Serializable
@JvmInline
value class IngredientId(@Serializable(with = UUIDSerializer::class) val value: UUID = UUID.randomUUID())

@Serializable
@JvmInline
value class IngredientName(val value: String)

@Serializable
@JvmInline
value class IngredientQuantity(val value: Int)

@Serializable
@JvmInline
value class IngredientInputTime(@Serializable(with = BigDecimalSerializer::class) val value: BigDecimal)

@Serializable
data class IngredientItem(
    val id: IngredientId,
    val name: IngredientName,
    val quantity: IngredientQuantity,
    val inputTime: IngredientInputTime,
)

@Serializable
@JvmInline
value class GameId(@Serializable(with = UUIDSerializer::class) val value: UUID = UUID.randomUUID())

@Serializable
@JvmInline
value class GameName(val value: String)

@Serializable
enum class GameStatus {
    CREATED, PREPARED, STARTED, COMPLETED
}
