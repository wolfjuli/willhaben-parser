package solutions.lykos.willhaben.parser.backend.importer.pipelines

import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import java.io.Writer
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

sealed class PipelineMessage<T : Node>(
    val uuid: Int = uuidCounter.getAndIncrement(),
    val flags: EnumSet<Flags> = EnumSet.noneOf(Flags::class.java)
) {
    companion object {
        private val uuidCounter = AtomicInteger(0)
    }

    override fun equals(other: Any?): Boolean =
        other is PipelineMessage<*> && other.uuid == uuid

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

    enum class Flags {
        WRITE,
        MULTIPLIED,
        FLUSH
    }

    fun hasFlag(flag: Flags) = flag in flags

    val hasWriteFlag
        get() = hasFlag(Flags.WRITE)

    abstract class SimpleMessage<T : Node>(
        uuid: Int = uuidCounter.getAndIncrement(),
        flags: EnumSet<Flags> = EnumSet.noneOf(Flags::class.java)
    ) : PipelineMessage<T>(uuid, flags)

    open class Payload<T : Node>(
        uuid: Int = uuidCounter.getAndIncrement(),
        val payload: T,
        flags: EnumSet<Flags> = EnumSet.noneOf(Flags::class.java)
    ) : SimpleMessage<T>(uuid, flags.clone()) {

        override fun toString(): String = "${javaClass.simpleName}<$payload>"

        constructor(
            payload: T
        ) : this(uuidCounter.getAndIncrement(), payload)

        constructor(copyFrom: Payload<T>, payload: T) :
                this(copyFrom.uuid, payload, copyFrom.flags.clone())

        constructor(payload: T, flags: EnumSet<Flags>) :
                this(uuidCounter.getAndIncrement(), payload, flags.clone())


        fun <R : Node> copy(newPayload: R): Payload<R> = Payload(uuid, newPayload, flags.clone())
    }

    open class Stop<T : Node> : SimpleMessage<T>()
    open class Update<T : Node>(flags: EnumSet<Flags> = EnumSet.noneOf(Flags::class.java)) :
        SimpleMessage<T>(flags = flags)

    open class Close<T : Node> : SimpleMessage<T>()
    open class Report<T : Node>(val writer: Writer) : SimpleMessage<T>()

    open class Bulk<T : Node>(
        val messages: List<PipelineMessage<T>>,
        uuid: Int = uuidCounter.getAndIncrement(),
        flags: EnumSet<Flags> = EnumSet.noneOf(Flags::class.java)
    ) : PipelineMessage<T>(uuid, flags) {

        fun <R : Node> copy(newPayloadTransform: T.() -> R): Bulk<R> = Bulk(messages.mapNotNull {
            when (it) {
                is Payload<T> -> it.copy(it.payload.newPayloadTransform()).takeIf { it !is Stop<*> }
                is Bulk<T> -> it.copy(newPayloadTransform)
                else -> null
            }
        }, uuid, flags.clone())

        fun simplify(): PipelineMessage<T> =
            when (messages.size) {
                0 -> Stop()
                1 -> messages.first()
                else -> this
            }

        fun add(vararg messages: PipelineMessage<T>) = Bulk(this.messages + messages.toList(), uuid, flags)


        override fun toString(): String = "${javaClass.simpleName}<${messages.size} messages>"
    }
}
