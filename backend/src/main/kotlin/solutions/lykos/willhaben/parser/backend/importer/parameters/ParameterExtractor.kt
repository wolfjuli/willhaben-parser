package solutions.lykos.willhaben.parser.backend.importer.parameters

/**
 * @author Gunnar Schulze
 */
class ParameterExtractor private constructor(
    private val patternExpression: Regex,
    private val parameterNames: Set<String>
) {
    companion object {
        private val parameterRegex = Regex("\\{([^}]+)}|\\*{1,2}")

        fun fromPattern(pattern: String): ParameterExtractor {
            val parameterNames = parameterRegex.findAll(pattern).mapNotNull { it.groups[1]?.value }.toSet()

            val encounteredParamNames = HashSet<String>()
            val builder = StringBuilder()
            var prevEnd = 0
            for (matchResult in parameterRegex.findAll(pattern)) {
                val range = matchResult.range
                if (prevEnd < range.first)
                    builder.append(Regex.escape(pattern.substring(prevEnd, range.first)))
                val paramName = matchResult.groupValues[1]
                if (paramName.isNotEmpty()) {
                    if (paramName in encounteredParamNames) {
                        builder.append("\\k<$paramName>")
                    } else {
                        val paramPattern = ParameterRegistry.patterns[paramName]
                            ?: throw IllegalArgumentException("Unknown parameter '$paramName'")
                        builder.append("(?<$paramName>$paramPattern)")
                        encounteredParamNames.add(paramName)
                    }
                } else {
                    val wildcard = matchResult.groupValues[0]
                    builder.append(if (wildcard == "*") "[^/]*" else ".*")
                }
                prevEnd = range.last + 1
            }
            if (prevEnd < pattern.length)
                builder.append(Regex.escape(pattern.substring(prevEnd, pattern.length)))
            return ParameterExtractor(Regex(builder.toString()), parameterNames)
        }
    }

    fun extract(entry: String): Parameters? {
        return patternExpression.matchEntire(entry)?.groups?.let { groups ->
            val values = HashMap<String, String>()
            parameterNames.forEach { paramName ->
                val value = groups[paramName]?.value
                if (value != null)
                    values[paramName] = value
            }
            Parameters.extract(values)
        }
    }

    override fun toString(): String {
        return patternExpression.pattern
    }
}
