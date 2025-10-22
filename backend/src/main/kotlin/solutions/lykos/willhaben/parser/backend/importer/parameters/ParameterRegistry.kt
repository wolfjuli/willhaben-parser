package solutions.lykos.willhaben.parser.backend.importer.parameters

/**
 * @author Gunnar Schulze
 */
object ParameterRegistry {

    val parameters = Parameter::class.java.classLoader
        .getResources("META-INF/services/${Parameter::class.qualifiedName}")
        .asSequence()
        .flatMap { url -> url.openStream().bufferedReader().useLines { it.toList().asSequence() } }
        .map { className -> Class.forName(className).getField("INSTANCE").get(null) as Parameter<*> }
        .toSet()

    val patterns = parameters
        .asSequence()
        .flatMap { it.patterns.asSequence() }
        .associateBy(Map.Entry<String, String>::key, Map.Entry<String, String>::value)
}
