package solutions.lykos.willhaben.parser.backend.database

import solutions.lykos.willhaben.parser.backend.camelCase
import solutions.lykos.willhaben.parser.backend.database.postgresql.get
import solutions.lykos.willhaben.parser.backend.database.postgresql.useAsSequence
import solutions.lykos.willhaben.parser.backend.database.postgresql.useTransaction
import java.sql.Connection

open class Database(
    val basePath: String = "/solutions/lykos/willhaben/parser/queries",
    val connection: () -> Connection,
) {

    data class ColumnDef(
        val name: String,
        val type: String,
        val ordinal: Int,
        val isId: Boolean,
        val isAutoGenerated: Boolean,
    )

    data class TableDef(
        val name: String,
        val type: String,
        val columns: MutableList<ColumnDef> = mutableListOf()
    )

    val tables: Map<String, TableDef> = connection().useTransaction { transaction ->
        transaction.prepareStatement(
            """
            SELECT t.table_type,
                   t.table_name,
                   c.ordinal_position,
                   c.column_name,
                   CASE
                       WHEN c.data_type = 'ARRAY' THEN regexp_replace(c.udt_name, '[_]', '') || '[]'
                       WHEN c.data_type LIKE 'timestamp%' THEN c.udt_name
                       WHEN c.data_type LIKE 'character varying' THEN c.udt_name
                       WHEN c.data_type LIKE 'USER-DEFINED' THEN c.udt_name
                       ELSE c.data_type END    AS data_type,
                   tc.column_name IS NOT NULL AS is_id,
                   c.is_identity              AS is_auto_generated
            FROM information_schema.columns c
                     JOIN information_schema.tables t
                          ON c.table_catalog = t.table_catalog AND c.table_schema = t.table_schema AND c.table_name = t.table_name
                     LEFT JOIN (SELECT table_catalog, table_schema, table_name, column_name
                                FROM information_schema.key_column_usage ku
                                         JOIN information_schema.table_constraints tc
                                              USING (constraint_catalog, constraint_schema, constraint_name, table_catalog,
                                                     table_schema, table_name)
                                WHERE tc.constraint_type = 'PRIMARY KEY'
                                ORDER BY table_name, ordinal_position) tc
                               ON c.table_catalog = tc.table_catalog AND c.table_schema = tc.table_schema AND
                                  c.table_name = tc.table_name AND c.column_name = tc.column_name
            WHERE t.table_schema = 'willhaben'
            ORDER BY 1, 2, c.ordinal_position;
        """.trimIndent()
        ).executeQuery().useAsSequence { seq ->
            seq.groupBy(
                { TableDef(it.get("table_name"), it.get("table_type")) },
                {
                    ColumnDef(
                        it.get("column_name"),
                        it.get("data_type"),
                        it.get("ordinal_position"),
                        it.get("is_id"),
                        it.get("is_auto_generated")
                    )
                }
            )
                .asSequence()
                .associate { (table, cols) -> table.name to table.also { table.columns.addAll(cols) } }
        }
    }


    fun selectQuery(table: TableDef, limit: Int? = null, offset: Int? = null) = """
        SELECT ${table.columns.joinToString(",") { it.name }}
        FROM ${table.name}
        WHERE ${table.columns.joinToString(" OR ") { "(${'$'}{${it.name.camelCase()}}::${it.type}[] IS NULL OR ${it.name} = ANY(${'$'}{${it.name.camelCase()}}::${it.type}[]))" }}
        LIMIT $limit
        OFFSET $offset
    """.trimIndent()

    fun insertQuery(table: TableDef) = """
        INSERT INTO  ${table.name} (${table.columns.filter { !it.isAutoGenerated }.joinToString(",") { it.name }})
        VALUES (${
        table.columns.filter { !it.isAutoGenerated }.joinToString(",") { "${'$'}{${it.name.camelCase()}}::${it.type}" }
    })
        RETURNING ${table.columns.joinToString(",") { it.name }}
    """.trimIndent()

    fun updateQuery(table: TableDef) = """
        UPDATE  ${table.name} SET
        ${
        table.columns.filter { !it.isAutoGenerated }
            .joinToString(",") { "${it.name} = ${'$'}{${it.name.camelCase()}}::${it.type}" }
    }
        WHERE ${
        table.columns.filter { it.isId }
            .joinToString(" AND ") { "${it.name} = ${'$'}{${it.name.camelCase()}}::${it.type}" }
    }
        RETURNING ${table.columns.joinToString(",") { it.name }}
    """.trimIndent()

    fun deleteQuery(table: TableDef) = """
        DELETE FROM  ${table.name}
        WHERE ${
        table.columns.filter { it.isId }
            .joinToString(" AND ") { "${it.name} = ${'$'}{${it.name.camelCase()}}::${it.type}" }
    }
    """.trimIndent()

}
