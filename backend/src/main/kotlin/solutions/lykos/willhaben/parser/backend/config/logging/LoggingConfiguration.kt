package solutions.lykos.willhaben.parser.backend.config.logging


class LoggingConfiguration {
    var appenders: List<BaseAppenderConfiguration> = emptyList()
    var level: String = "INFO"
    val name: String = "Application"
}
