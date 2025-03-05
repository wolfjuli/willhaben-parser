package solutions.lykos.willhaben.parser.backend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import solutions.lykos.willhaben.parser.backend.api.API
import solutions.lykos.willhaben.parser.backend.assets.AssetProvider
import solutions.lykos.willhaben.parser.backend.assets.Svelte
import solutions.lykos.willhaben.parser.backend.config.ConnectorConfiguration
import solutions.lykos.willhaben.parser.backend.config.WPConfiguration
import solutions.lykos.willhaben.parser.backend.crawler.Crawler
import solutions.lykos.willhaben.parser.backend.logging.LoggingConfigurator
import solutions.lykos.willhaben.parser.backend.postgresql.DatabaseManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore

object Server {
    @JvmStatic
    fun main(args: Array<String>) {
        val configuration = readConfiguration(args)
        LoggingConfigurator.configure(configuration.logger)

        val databaseManager =
            DatabaseManager(
                javaClass.classLoader.getResource("solutions/lykos/willhaben/parser/sql")!!,
                listOf("postgis", "pgcrypto", "postgis_raster")
            )

        if (args.contains("setup")) {
            databaseManager.setup(configuration.database)
            return
        }

        if (args.contains("destroy")) {
            databaseManager.destroy(configuration.database)
            return
        }

        val server = createServer(configuration)
        val crawler = Crawler(configuration)

        crawler.start()
        server.start(true) // Blocking
        crawler.stop()
    }


    private fun readConfiguration(args: Array<String>): WPConfiguration {
        val startIndex = args.indexOf("-c")
        val configPath = if (args.size > startIndex + 1) args[startIndex + 1] else null

        if (startIndex == -1 || configPath == null)
            error("Invalid configuration: Usage: java -jar WillHaben.jar -c /path/to/configuration.yml")

        val objectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule().registerModule(JavaTimeModule())
        return objectMapper.readValue(File(configPath), WPConfiguration::class.java)
    }

    private fun createServer(
        configuration: WPConfiguration
    ): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
        return embeddedServer(Netty, configure = {
            configureConnector(configuration.server)
        }, module = {
            install(ContentNegotiation) {
                jackson()
            }
            configureRouting(configuration)
            configureSwagger()
        })
    }

    private fun BaseApplicationEngine.Configuration.configureConnector(config: ConnectorConfiguration) {
        config.ssl?.let { sslConfig ->
            val keyStoreFile = Paths.get(sslConfig.keyStorePath)
            val keyStore =
                KeyStore.getInstance("JKS").apply {
                    Files.newInputStream(keyStoreFile).use {
                        load(it, sslConfig.keyStorePassword.toCharArray())
                    }

                    requireNotNull(getKey(sslConfig.keyAlias, sslConfig.keyStorePassword.toCharArray()) == null) {
                        "The specified key ${sslConfig.keyAlias} doesn't exist in the key store ${sslConfig.keyStorePath}"
                    }
                }

            sslConnector(
                keyStore,
                sslConfig.keyAlias,
                { sslConfig.keyStorePassword.toCharArray() },
                { sslConfig.keyStorePassword.toCharArray() }
            ) {
                this@sslConnector.host = config.host
                this@sslConnector.port = config.port
                this.keyStorePath = keyStoreFile.toFile()
            }
        } ?: connector {
            this@connector.host = config.host
            this@connector.port = config.port
        }
    }

    private fun Application.configureSwagger() {
        routing {
            swaggerUI(path = "documentation")
        }
        routing {
            openAPI(path = "api/open")
        }
    }

    private fun Application.configureRouting(configuration: WPConfiguration) {

        install(API) {
            database = configuration.database
        }

        install(AssetProvider) {
            layout = Svelte
            resourcePath {
                "solutions/lykos/willhaben/parser/frontend"
            }
        }
    }
}
