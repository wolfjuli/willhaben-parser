package solutions.lykos.willhaben.parser.backend.geometry

typealias TranslatedField = MutableMap<String, String>

fun Map<String, TranslatedField>.toSimpleMap(): Map<String, String> =
    mutableMapOf<String, String>().let { new ->
        forEach { entry ->
            if (entry.value.size == 1)
                new[entry.key] = entry.value["default"]!!
            else
                entry.value.forEach { subEntry ->
                    val key = entry.key + (":${subEntry.key}".takeIf { subEntry.key != "default" } ?: "")
                    new[key] = subEntry.value
                }
        }

        new
    }

fun Collection<Feature>.toGeoJson(): String = """
    {
    "type":"FeatureCollection",
    "features": [${joinToString { GeoJSONConverter.toGeoJSON(it) }}]
    }
""".trimIndent()
