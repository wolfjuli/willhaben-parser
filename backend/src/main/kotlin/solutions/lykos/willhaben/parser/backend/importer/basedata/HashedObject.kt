package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.importer.Hash

data class HashedObject(
    private val identityCalculation: () -> String,
    private val hashCalculation: () -> Hash
) {
    private val _baseIdentity: String by lazy { identityCalculation() }
    private var _identity: String? = null

    var identity: String
        get() = _identity ?: _baseIdentity
        set(value) {
            _identity = value
        }

    val hash: Hash by lazy { hashCalculation() }
}
