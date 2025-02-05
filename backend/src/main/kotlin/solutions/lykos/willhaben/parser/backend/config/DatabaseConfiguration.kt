package solutions.lykos.willhaben.parser.backend.config

class DatabaseConfiguration : Credentials() {
    lateinit var name: String
    lateinit var host: String
    var port = 5432
    var searchPath: String = "public"
    var minimumIdleConnections: Int = 1
    var maximumPoolSize: Int = 10
    var idleTimeout: Int = 300

    lateinit var admin: Credentials
}

open class Credentials {

    lateinit var user: String
    lateinit var password: String
}
