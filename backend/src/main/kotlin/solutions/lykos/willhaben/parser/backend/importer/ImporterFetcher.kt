package solutions.lykos.willhaben.parser.backend.importer

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.importer.TableDefinitions.getTableName
import solutions.lykos.willhaben.parser.backend.importer.basedata.HashedObject
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.basedata.hashFields
import solutions.lykos.willhaben.parser.backend.importer.basedata.idFields
import solutions.lykos.willhaben.parser.backend.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.postgresql.useAsSequence

object ImporterFetcher {
    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

    inline fun <reified T : Node> fetchMapping(
        transaction: Transaction,
        tableName: String = getTableName<T>(),
        columnName: String = "external_id"
    ): MutableMap<String, Int> {
        return transaction.prepareStatement(
            """
            --[mapping] $tableName
            SELECT id, $columnName
            FROM $tableName
            """.trimIndent()
        ).useAsSequence { seq ->
            seq.map {
                it.getString(columnName) to it.getInt("id")
            }.toMap(LinkedHashMap())
        }
    }

    inline fun <reified T : Node> fetchHashes(
        transaction: Transaction,
        knownObjects: MutableMap<Hash, HashedObject> = mutableMapOf(),
        tableName: String = getTableName<T>(),
        hashColumns: Set<String> = hashFields<T>(),
        idColumns: Set<String> = idFields<T>()
    ): MutableMap<Hash, HashedObject> =
        knownObjects.let { hashes ->
            """
            --[hashes] $tableName
            WITH col_info AS (
                SELECT isc.column_name              AS column_name,
                       col_name.camel_case          AS camel_case_column_name,
                       hash.column_name IS NOT NULL AS is_hash_column,
                       lower(isc.data_type)         AS column_type
                FROM information_schema.columns AS isc
                LEFT JOIN unnest('{${hashColumns.joinToString { it.toSnakeCase() }}}'::TEXT[]) hash(column_name)
                    ON isc.column_name = hash.column_name
                LEFT JOIN unnest('{${idColumns.joinToString { it.toSnakeCase() }}}'::TEXT[]) i(column_name)
                    ON isc.column_name = i.column_name,
                     LATERAL (SELECT lower(left(str, 1)) || right(str, -1) AS camel_case
                              FROM replace(
                                           initcap(regexp_replace(isc.column_name, '_([a-z0-9])', ' \1', 'g'))
                                       , ' ', '') AS str) AS col_name
                WHERE isc.table_name = '$tableName'
                AND coalesce(i.column_name, hash.column_name) IS NOT NULL
            ),
                 jsons AS (
                     SELECT row_number() over ()  AS id,
                            to_jsonb(v)           AS json
                     FROM (SELECT *,
                       ${(hashColumns).joinToString { "pg_typeof(" + it.toSnakeCase() + ") as _${it.toSnakeCase()}_type" }}
                     FROM $tableName) AS v
                 ),
                 aggs AS (
                     SELECT j.id,
                            j.json,
                            c.is_hash_column,
                            string_agg(coalesce(CASE
                                                    WHEN j.json ->> ('_' || c.column_name || '_type') in ('geometry', 'geography')
                                                        THEN regexp_replace(st_asgeojson((j.json ->> c.column_name)::geometry),'[^0-9]',
                                                                            '', 'g')
                                                    WHEN c.column_type = 'array' THEN (SELECT array_agg(x.e)::TEXT
                                                                           FROM jsonb_array_elements_text((j.json -> c.column_name)) x(e))
                                                    ELSE (j.json ->> c.column_name)::TEXT END, ''), ','
                                       ORDER BY camel_case_column_name) AS col_vals
                     FROM col_info c
                              CROSS JOIN jsons j
                     GROUP BY j.id, j.json, c.is_hash_column
                 )
            SELECT a.col_vals                                     AS identity,
                   encode(sha512(b.col_vals::TEXT::BYTEA), 'hex')::TEXT AS hash
            FROM aggs a
                     JOIN aggs b
                          ON a.id = b.id
                              AND a.is_hash_column = FALSE
                              AND b.is_hash_column = TRUE
                              ;
            """.trimIndent().let { query ->
                if (logger.isDebugEnabled) {
                    logger.debug(query)
                }

                transaction
                    .prepareStatement(query)
                    .useAsSequence { seq ->
                        seq.forEach { item ->
                            val hash = item.getString("hash")
                            if (hash !in hashes) {
                                val identity = item.getString("identity")
                                hashes[hash] =
                                    HashedObject({ identity }, { hash })
                            }
                        }
                        hashes
                    }
            }
        }
}
