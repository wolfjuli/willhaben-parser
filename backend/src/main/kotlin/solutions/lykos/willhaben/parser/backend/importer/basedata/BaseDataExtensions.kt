package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.importer.annotations.HashField
import solutions.lykos.willhaben.parser.backend.importer.annotations.IdField
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


inline fun <reified T : Node> getMemberPropertyNames(
    noinline filter: ((KProperty1<out Node, *>) -> Boolean) = { true }
): Set<String> = T::class.memberProperties
    .asSequence()
    .filter(filter)
    .map { it.name }
    .toSet()

fun KProperty1<out Node, *>.isHashField(): Boolean = this.annotations.any { it is HashField }
fun KProperty1<out Node, *>.isIdField(): Boolean = this.annotations.any { it is IdField }

inline fun <reified T : Node> hashFields(): Set<String> = getMemberPropertyNames<T> { it.isHashField() }
inline fun <reified T : Node> idFields(): Set<String> = getMemberPropertyNames<T> { it.isIdField() }
    .takeIf { it.isNotEmpty() } ?: getMemberPropertyNames<T> { !it.isHashField() }

