package solutions.lykos.willhaben.parser.backend.importer.parameters

/**
 * @author Gunnar Schulze
 */
abstract class Parameter<T : Any>(val name: String) {

    abstract val patterns: Map<String, String>

    open fun normalize(value: T): T = value

    abstract fun extract(values: Map<String, String>): T?

    final override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Parameter<*>

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
