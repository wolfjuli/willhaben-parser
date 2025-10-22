package solutions.lykos.willhaben.parser.backend.importer

import solutions.lykos.willhaben.parser.backend.importer.basedata.Node

object TableDefinitions {

    data class TableDefinition(
        val node: Class<out Node>,
        val tableName: String
    )

    private val baseTableDefinitions: Set<TableDefinition> = setOf(
        TableDefinition(Node::class.java, "Node"),
    )

    val tableDefinitions: MutableSet<TableDefinition> = baseTableDefinitions.toMutableSet()

    fun resetTableDefinitions(): Boolean = tableDefinitions.removeAll { it !in baseTableDefinitions }
    fun addTableDefinition(tableDefinition: TableDefinition): Boolean = tableDefinitions.add(tableDefinition)
    fun addTableDefinitions(tableDefinitions: Collection<TableDefinition>): Boolean =
        TableDefinitions.tableDefinitions.addAll(tableDefinitions)

    inline fun <reified T : Node> getTableName(): String = getTableName(T::class.java)

    fun <T : Node> getTableName(recordClass: Class<T>): String =
        tableDefinitions.find { it.node == recordClass }?.tableName
            ?: recordClass.simpleName.toSnakeCase().toPlural()
            ?: error("There is no table name specified for Node type ${recordClass.simpleName}")
}
