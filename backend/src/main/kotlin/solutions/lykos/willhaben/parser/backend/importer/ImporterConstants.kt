package solutions.lykos.willhaben.parser.backend.importer

object ImporterConstants {
    const val PSQL_BATCH_SIZE = 1000
    const val PG_MAX_IDENTIFIER_LENGTH = 63
    const val PG_TYPE_STRING = "TEXT"
    const val PG_TYPE_DATE = "DATE"
    const val PG_TYPE_BOOLEAN = "BOOLEAN"
    const val PG_TYPE_INTEGER = "INTEGER"
    const val PG_TYPE_BIGINTEGER = "BIGINT"
    const val PG_TYPE_DOUBLE = "DOUBLE PRECISION"
    const val PG_TYPE_FLOAT = "REAL"
    const val PG_TYPE_BYTEA = "BYTEA"
    const val PG_TYPE_JSONB = "JSONB"
    const val PG_CREATE_TEMP_TABLE = "CREATE TEMPORARY TABLE "

    const val CHANGE_FILE_NAME = "change.json"

    const val PIPELINE_BUFFER_SIZE = PSQL_BATCH_SIZE
}
