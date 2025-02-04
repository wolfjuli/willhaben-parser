package solutions.lykos.willhaben.parser.backend.config

import solutions.lykos.willhaben.parser.backend.config.logging.LoggingConfiguration

class WPConfiguration {
    lateinit var server: ConnectorConfiguration
    lateinit var database: DatabaseConfiguration
    var logger: LoggingConfiguration = LoggingConfiguration()
}
