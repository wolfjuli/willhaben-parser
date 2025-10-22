package solutions.lykos.willhaben.parser.backend.database.postgresql

import java.net.URL

class QueryTemplateProvider(
    private val queryBaseURL: URL
) {
    private val cachedTemplates = HashMap<String, String>()
    private val cachedExpressions = HashMap<Set<String>, Regex>()

    fun getTemplate(
        path: String,
        parameters: Map<String, Any> = emptyMap()
    ): String {
        val baseTemplate =
            cachedTemplates.getOrPut(path) {
                val url =
                    URL(queryBaseURL.protocol, queryBaseURL.host, queryBaseURL.port, "${queryBaseURL.file}/$path.sql")
                url.openStream().bufferedReader().use { reader -> reader.readText() }
            }
        return if (parameters.isEmpty()) {
            baseTemplate
        } else {
            val paramNames = parameters.keys
            val expression =
                cachedExpressions.getOrPut(paramNames) {
                    Regex(paramNames.joinToString("|", "\\$\\{(", ")\\}"))
                }
            expression.replace(baseTemplate) { parameters.getValue(it.groupValues[1]).toString() }
        }
    }
}
