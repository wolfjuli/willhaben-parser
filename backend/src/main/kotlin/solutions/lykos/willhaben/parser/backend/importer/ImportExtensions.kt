package solutions.lykos.willhaben.parser.backend.importer

import org.slf4j.Logger
import solutions.lykos.willhaben.parser.backend.importer.basedata.HashType
import solutions.lykos.willhaben.parser.backend.importer.basedata.Node
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import solutions.lykos.willhaben.parser.backend.jsonObjectMapper
import java.io.InputStreamReader
import java.math.BigInteger
import java.security.MessageDigest
import java.sql.PreparedStatement
import java.util.Collections.synchronizedSet
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

val jacksonMapper = jsonObjectMapper()

fun ZipEntry.getInputStreamReader(zipFile: ZipFile): InputStreamReader =
    zipFile.getInputStream(this).reader()

fun Sequence<ZipEntry>.filterUnwantedFiles(): Sequence<ZipEntry> =
    filterNot {
        it.name.startsWith("__MACOSX") ||
                it.name.substringAfterLast("/").startsWith(".")
    }

fun hashString(value: String, hashType: HashType): String = when (hashType) {
    HashType.SHA512 -> sha512(value)
}

private fun sha512(value: String): String {
    val md: MessageDigest = MessageDigest.getInstance("SHA-512")
    val messageDigest = md.digest(value.toByteArray(Charsets.UTF_8))
    // Convert byte array into signum representation
    val no = BigInteger(1, messageDigest)
    // Convert message digest into hex value
    var hashtext: String = no.toString(16)
    // Add preceding 0s to make it 128 chars long
    while (hashtext.length < 128) {
        hashtext = "0$hashtext"
    }
    return hashtext
}

private val upperCaseChar = "(?<=.)(?=\\p{Upper})".toRegex()
fun String.toSnakeCase() = replace(upperCaseChar, "_").lowercase()
fun String.toPlural() = this + (this.last().let { l -> "s".takeIf { l != 's' } } ?: "")

class KeyNotFoundException(key: String) : Exception("Key '$key' not found in map")

fun <K : Any, V : Any> Map<K, V>.getOrError(key: K): V =
    getOrElse(key) { throw KeyNotFoundException(key.toString()) }

val String?.orNull: String?
    get() = this?.takeIf { it.isNotEmpty() && it.lowercase().trim() != "null" }

@JvmName("getOrErrorNullKey")
fun <K : Any, V : Any> Map<K, V>.getOrError(key: K?): V? =
    key?.let { k -> getOrElse(k) { throw KeyNotFoundException(key.toString()) } }

@JvmName("getOrErrorNullKeyValue")
fun <K : Any, V : Any?> Map<K, V>.getOrError(key: K?): V? =
    key?.let { k -> if (k in this) getValue(key) else throw KeyNotFoundException(key.toString()) }

fun <T : Node> Collection<PipelineMessage<T>>.toBulk() = PipelineMessage.Bulk(this.toList())

typealias BufferList<T> = HashSet<PipelineMessage.Payload<T>>

fun <T : Node> emptyBufferList(size: Int = ImporterConstants.PIPELINE_BUFFER_SIZE):
        MutableSet<PipelineMessage.Payload<T>> =
    synchronizedSet(BufferList(size))

fun <T : Any> T?.orNotResolved(obj: Any): T = this
    ?: error("Fatal error: resolver did not do it's job of resolving: $obj")

fun <F : Node, T : Node> PipelineMessage<F>.matchType(payloadTransformer: F.() -> T): PipelineMessage<T> =
    when (this) {
        is PipelineMessage.Stop<F> -> PipelineMessage.Stop()
        is PipelineMessage.Update<F> -> PipelineMessage.Update()
        is PipelineMessage.Payload<F> -> this.copy(payloadTransformer(this.payload))
        is PipelineMessage.Bulk<F> -> this.copy { payloadTransformer(this) }
        is PipelineMessage.Close<F> -> PipelineMessage.Close()
        is PipelineMessage.Report<F> -> PipelineMessage.Report(writer)
        else -> error("Unknown message type: ${javaClass.simpleName}")
    }

/**
 * Executes a java.sql.PreparedStatement in a one-shot fashion.
 * Should only be used for executing single statements
 */
fun executeStatement(
    preparedStatement: PreparedStatement,
    logger: Logger? = null,
    block: (PreparedStatement) -> Unit
): Boolean =
    preparedStatement.use { statement ->
        block(statement)
        val res = statement.execute()

        statement.warnings?.asSequence()?.forEach { warning ->
            logger?.warn("[SQL]: ${warning.message}")
        }

        res
    }
