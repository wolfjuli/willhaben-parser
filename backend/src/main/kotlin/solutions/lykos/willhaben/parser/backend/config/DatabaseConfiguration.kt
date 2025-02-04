package solutions.lykos.willhaben.parser.backend.config

class DatabaseConfiguration {
    lateinit var name: String
    lateinit var host: String
    var port = 5432
    lateinit var user: String
    lateinit var password: String
    var searchPath: String = "lamar, public"
    var minimumIdleConnections: Int = 1
    var maximumPoolSize: Int = 10
    var idleTimeout: Int = 300
}
