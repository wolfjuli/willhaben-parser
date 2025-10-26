package solutions.lykos.willhaben.parser.backend.api

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import solutions.lykos.willhaben.parser.backend.config.DatabaseConfiguration
import solutions.lykos.willhaben.parser.backend.routing.API_PROVIDER_KEY_ID
import kotlin.time.Duration.Companion.seconds
import io.ktor.websocket.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json
import org.postgresql.ds.PGSimpleDataSource
import solutions.lykos.willhaben.parser.backend.database.Database
import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryTemplateProvider

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
            pipeline.install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            val dataSource = PGSimpleDataSource().apply {
                databaseName = configuration.database.name
                serverNames = arrayOf(configuration.database.host)
                portNumbers = intArrayOf(configuration.database.port)
                user = configuration.database.user
                password = configuration.database.password
            }

            val database = Database { dataSource.connection }
            val templates = QueryTemplateProvider(
                this::class.java.getResource("/solutions/lykos/willhaben/parser/queries")
                    ?: error("Base query url doesn't exist")
            )
            pipeline.routing {
                webSocket ("/api/v1/ws") {
                    wsRoutes(database, templates)
                }
            }

            return api
        }

    }
}
