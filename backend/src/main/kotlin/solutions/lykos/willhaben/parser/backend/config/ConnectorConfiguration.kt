package solutions.lykos.willhaben.parser.backend.config

class ConnectorConfiguration {
    lateinit var host: String
    var port: Int = 0
    var ssl: SSLConfiguration? = null

    override fun toString(): String = "${if (ssl != null) "https" else "http"}://$host:$port"
}
