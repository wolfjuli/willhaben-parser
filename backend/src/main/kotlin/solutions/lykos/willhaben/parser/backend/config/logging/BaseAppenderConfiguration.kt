package solutions.lykos.willhaben.parser.backend.config.logging

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(ConsoleAppenderConfiguration::class, name = "console"),
    JsonSubTypes.Type(FileAppenderConfiguration::class, name = "file")
)
abstract class BaseAppenderConfiguration {
    var filters: List<String> = listOf("all")
    var logFormat: String = "%-5marker [%d{ISO8601,UTC}] [%p] %m%n%rEx"
}
