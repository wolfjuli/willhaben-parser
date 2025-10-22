package solutions.lykos.willhaben.parser.backend.importer.actions.writers.copy

import solutions.lykos.willhaben.parser.backend.importer.ImporterConstants
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node

interface ValueDef<T : Node> {
    val sqlType: String
    val extractor: T.() -> Any?
    val copy: Boolean
}

open class PGString<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) : ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_STRING
}

open class PGBoolean<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) : ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_BOOLEAN
}

open class PGInteger<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) : ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_INTEGER
}

open class PGBigInteger<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) :
    ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_BIGINTEGER
}

open class PGDouble<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) : ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_DOUBLE
}

open class PGFloat<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) : ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_FLOAT
}

open class PGDate<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) : ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_DATE
}

open class PGByteA<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) : ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_BYTEA
}

open class PGJsonb<T : Node>(override val copy: Boolean = true, override val extractor: T.() -> Any?) : ValueDef<T> {
    override val sqlType: String = ImporterConstants.PG_TYPE_JSONB
}

class AsArray<R : Node, T : ValueDef<R>>(private val base: T) : ValueDef<R> {
    private val _sqlType = base.sqlType + "[]"
    override val sqlType: String
        get() = _sqlType

    override val copy: Boolean
        get() = base.copy

    override val extractor: R.() -> Any?
        get() = base.extractor
}
