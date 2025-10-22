package solutions.lykos.willhaben.parser.backend.api

import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import solutions.lykos.willhaben.parser.backend.config.DatabaseConfiguration
import solutions.lykos.willhaben.parser.backend.routing.API_PROVIDER_KEY_ID

class API(val configuration: Configuration) {

    class Configuration {
        lateinit var database: DatabaseConfiguration
    }

    companion object Feature : BaseApplicationPlugin<Application, Configuration, API> {

        // Creates a unique key for the feature.
        override val key = AttributeKey<API>(API_PROVIDER_KEY_ID)
        override fun install(pipeline: Application, configure: Configuration.() -> Unit): API {
            val configuration = Configuration().apply(configure)
            val api = API(configuration)

            pipeline.routing {
                route("/api/rest/v1") {
                    apiRoutes(configuration)
                }
            }

            return api
        }

    }
}
