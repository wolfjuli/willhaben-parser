package solutions.lykos.willhaben.parser.backend.database.postgresql

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.postgresql.ds.PGSimpleDataSource
import solutions.lykos.willhaben.parser.backend.config.DatabaseConfiguration
import solutions.lykos.willhaben.parser.backend.config.DatabaseCredentials
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.ProcessBuilder.Redirect
import java.util.function.Supplier

class DatabaseManager(
    basePath: String,
    private val extensions: List<String> = emptyList()
) {
    companion object {
        private val PATCH_REGEX = Regex("(?:.*/)?((\\d+)_.*\\.sql)")
    }

    private val latestPatchNumber: Int
        get() {
            return patchesUrl

                .let(FileResourceLoader::listResources)
                .maxOfOrNull { PATCH_REGEX.matchEntire(it.toString())!!.groupValues[2].toInt() } ?: 0
        }

    private val patchesUrl = javaClass.classLoader.getResource("$basePath/patches")
    private val schemaUrl = javaClass.classLoader.getResource("$basePath/schema")


    fun setup(
        databaseConfiguration: DatabaseConfiguration
    ): Boolean =
        create(databaseConfiguration, databaseConfiguration.admin) &&
                createDatabaseExtensions(databaseConfiguration, databaseConfiguration.admin) &&
                createSchema(databaseConfiguration)

    fun destroy(
        databaseConfiguration: DatabaseConfiguration
    ): Boolean {
        System.err.println("Destroying database...")
        val database = databaseConfiguration.name.lowercase().escapeSql()
        val user = databaseConfiguration.user.escapeSql()

        val databaseExists =
            ByteArrayOutputStream().use { stdout ->
                val success =
                    runPsql(
                        databaseConfiguration.host,
                        databaseConfiguration.port,
                        databaseConfiguration.admin.user,
                        databaseConfiguration.admin.password,
                        quiet = true,
                        stdout = stdout
                    ) {
                        "SELECT exists(SELECT FROM pg_catalog.pg_database AS pd WHERE lower(pd.datname) = '$database')"
                            .byteInputStream()
                    }
                if (!success) {
                    return false
                }

                stdout.toString().trim() == "t"
            }

        if (databaseExists) {
            val success =
                runPsql(
                    databaseConfiguration.host,
                    databaseConfiguration.port,
                    databaseConfiguration.admin.user,
                    databaseConfiguration.admin.password
                ) {
                    """
                REVOKE CONNECT ON DATABASE "$database" FROM PUBLIC;
                SELECT pg_terminate_backend(psa.pid)
                FROM pg_stat_activity AS psa
                WHERE psa.pid != pg_backend_pid()
                  AND psa.datname = '$database';
                """.byteInputStream()
                }

            if (!success) {
                return false
            }
        }

        return runPsql(
            databaseConfiguration.host,
            databaseConfiguration.port,
            databaseConfiguration.admin.user,
            databaseConfiguration.admin.password
        ) {
            """
            DROP DATABASE IF EXISTS "$database";
            DROP USER IF EXISTS "$user";
            """.byteInputStream()
        }
    }

    fun upgrade(
        databaseConfiguration: DatabaseConfiguration,
        dryRun: Boolean = false
    ): Boolean {
        val upgradeFrom = fetchCurrentPatchLevel(databaseConfiguration)
        return runPsql(
            databaseConfiguration.host,
            databaseConfiguration.port,
            databaseConfiguration.user,
            databaseConfiguration.password,
            databaseConfiguration.name
        ) {
            inputStreamOf(
                Supplier { "BEGIN;\n\n".byteInputStream() },
                Supplier {
                    inputStreamOf(
                        patchesUrl
                            .let(FileResourceLoader::listResources)
                            .mapNotNull { patchUrl ->
                                val (patchFile, patchLevel) =
                                    PATCH_REGEX
                                        .matchEntire(patchUrl.toString())!!
                                        .groupValues
                                        .let { it[1] to it[2].toInt() }
                                if (patchLevel in (upgradeFrom + 1)..latestPatchNumber) {
                                    Triple(patchLevel, patchFile, patchUrl)
                                } else {
                                    null
                                }
                            }.sortedBy { (patchLevel, _, _) ->
                                patchLevel
                            }.asSequence()
                            .flatMap { (patchLevel, patchFile, patchUrl) ->
                                sequenceOf(
                                    Supplier {
                                        (
                                                ";\n DO \$\$ BEGIN RAISE INFO 'Applying patch $patchLevel'; END; \$\$;\n"
                                                ).byteInputStream()
                                    },
                                    Supplier { patchUrl.openStream().buffered() },
                                    Supplier {
                                        (
                                                ";\n INSERT INTO schema_version(patch_level, patch_file) " +
                                                        "VALUES ($patchLevel, '$patchFile');\n"
                                                ).byteInputStream()
                                    }
                                )
                            }
                    )
                },
                { (if (dryRun) "ROLLBACK;\n\n" else "COMMIT;\n\n").byteInputStream() }
            )
        }
    }

    private fun create(
        databaseConfiguration: DatabaseConfiguration,
        adminCredentials: DatabaseCredentials
    ): Boolean {
        System.err.println("Creating database...")
        return runPsql(
            databaseConfiguration.host,
            databaseConfiguration.port,
            adminCredentials.user,
            adminCredentials.password
        ) {
            val database = databaseConfiguration.name.lowercase().escapeSql()
            val user = databaseConfiguration.user.escapeSql()
            val password = databaseConfiguration.password.escapeSql()
            val searchPath =
                databaseConfiguration.searchPath
                    .split(Regex("\\s*,\\s*"))
                    .joinToString(", ") { it.escapeSql() }
            StringBuilder()
                .apply {
                    append(
                        """
                        CREATE USER "$user" WITH PASSWORD '$password';
                        CREATE DATABASE "$database" OWNER "$user";
                        ALTER DATABASE "$database" SET search_path TO $searchPath;
                        """.trimIndent()
                    )
                    append(
                        """
                        REVOKE ALL PRIVILEGES ON DATABASE "$database" FROM PUBLIC;
                        GRANT ALL PRIVILEGES ON DATABASE "$database" TO "$user";
                        """.trimIndent()
                    )
                }.toString()
                .byteInputStream()
        }
    }

    private fun createSchema(databaseConfiguration: DatabaseConfiguration): Boolean =

        (if (databaseConfiguration.searchPath.contains(","))
            runPsql(
                databaseConfiguration.host,
                databaseConfiguration.port,
                databaseConfiguration.user,
                databaseConfiguration.password,
                databaseConfiguration.name
            ) {

                inputStreamOf(
                    Supplier {
                        val schema =
                            databaseConfiguration.searchPath
                                .substringBefore(',')
                                .trim()
                                .escapeSql()
                        "\n\nCREATE SCHEMA $schema\n\n;".byteInputStream()
                    }
                )
            } else true) && runSchema(databaseConfiguration)

    private fun runSchema(
        databaseConfiguration: DatabaseConfiguration
    ): Boolean {
        return runPsql(
            databaseConfiguration.host,
            databaseConfiguration.port,
            databaseConfiguration.user,
            databaseConfiguration.password,
            databaseConfiguration.name
        ) {
            inputStreamOf(
                Supplier { "BEGIN; \n\n".byteInputStream() },
                Supplier {
                    schemaUrl
                        .let(FileResourceLoader::listResources)
                        .asSequence()
                        .sortedBy { it.toExternalForm() }
                        .joinToString("\n\n") { it.readText() }
                        .byteInputStream()
                },
                Supplier { "COMMIT;".byteInputStream() }
            )
        }
    }


    private fun createDatabaseExtensions(
        databaseConfiguration: DatabaseConfiguration,
        adminCredentials: DatabaseCredentials
    ): Boolean =
        runPsql(
            databaseConfiguration.host,
            databaseConfiguration.port,
            adminCredentials.user,
            adminCredentials.password,
            databaseConfiguration.name
        ) {
            extensions.joinToString("") { "CREATE EXTENSION IF NOT EXISTS $it;\n" }.byteInputStream()
        }

    private fun fetchCurrentPatchLevel(databaseConfiguration: DatabaseConfiguration): Int = try {
        PGSimpleDataSource()
            .apply {
                databaseName = databaseConfiguration.name
                serverNames = arrayOf(databaseConfiguration.host)
                portNumbers = intArrayOf(databaseConfiguration.port)
                user = databaseConfiguration.user
                password = databaseConfiguration.password
            }.connection
            .use { connection ->
                connection.prepareStatement("SELECT max(patch_level) FROM schema_version").use { statement ->
                    statement.executeQuery().use {
                        if (it.next()) it.getInt(1) else 0
                    }
                }
            }
    } catch (e: Exception) {
        0
    }

    private fun runPsql(
        host: String,
        port: Int,
        user: String,
        password: String,
        database: String? = null,
        quiet: Boolean = false,
        stdout: OutputStream = System.out,
        stderr: OutputStream = System.err,
        stdin: () -> InputStream
    ): Boolean {
        val args =
            arrayListOf(
                "psql",
                "-v",
                "ON_ERROR_STOP=1",
                "-A",
                "-t",
                "-h",
                host,
                "-p",
                port.toString(),
                "-U",
                user
            )
        if (database != null) {
            args.add("-d")
            args.add(database)
        }
        if (quiet) {
            args.add("-q")
        }
        return runProcess(
            args.toTypedArray(),
            mapOf("PGPASSWORD" to password),
            stdin = stdin(),
            stdout = stdout,
            stderr = stderr
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun runProcess(
        command: Array<String>,
        environment: Map<String, String>,
        stdin: InputStream? = null,
        stdout: OutputStream = System.out,
        stderr: OutputStream = System.err
    ): Boolean =
        try {
            val process =
                with(ProcessBuilder().command(*command)) {
                    environment().putAll(environment)

                    redirectInput(Redirect.PIPE)
                    redirectOutput(if (stdout == System.out) Redirect.INHERIT else Redirect.PIPE)
                    redirectError(if (stderr == System.err) Redirect.INHERIT else Redirect.PIPE)

                    start()
                }

            if (stdin != null) GlobalScope.launch { process.outputStream.use(stdin::copyTo) }
            if (stdout != System.out) GlobalScope.launch { process.inputStream.copyTo(stdout) }
            if (stderr != System.err) GlobalScope.launch { process.errorStream.copyTo(stderr) }

            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
}
