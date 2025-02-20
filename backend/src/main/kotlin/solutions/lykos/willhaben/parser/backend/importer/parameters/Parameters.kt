package solutions.lykos.willhaben.parser.backend.importer.parameters

/**
 * @author Gunnar Schulze
 */
class Parameters(private val data: HashMap<Parameter<*>, Any> = HashMap()) {

    companion object {
        fun extract(values: Map<String, String>): Parameters {
            val data = HashMap<Parameter<*>, Any>()
            ParameterRegistry.parameters.forEach { parameter ->
                parameter.extract(values)?.let { value ->
                    data[parameter] = value
                }
            }
            return Parameters(data)
        }
    }

    fun <T : Any> with(parameter: Parameter<T>, value: T): Parameters {
        data[parameter] = value
        return this
    }

    operator fun <T : Any> get(parameter: Parameter<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return data[parameter] as T?
    }

    operator fun <T : Any> set(parameter: Parameter<T>, value: T?) {
        if (value != null)
            data[parameter] = value
        else
            data.remove(parameter)
    }

    override fun toString(): String {
        return data.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Parameters) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }
}
