package solutions.lykos.willhaben.parser.backend.assets

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.Reader

/**
 * @author Clemens Meinhart
 */
abstract class AssetLayout {
    abstract val assetsPath: String
    abstract val profileImagePathPattern: String

    open val legacyAssetPath: String
        get() = assetsPath
    open val legacyProfileImagePathPattern: String
        get() = profileImagePathPattern

    abstract fun resolveResource(
        path: String,
        themeId: String
    ): Resource?

    abstract fun filterHtml(
        content: Reader,
        resolveParameters: Map<String, () -> String?>
    ): Reader

    open fun filterResource(
        resource: OutgoingContent.ReadChannelContent,
        resolveParameters: Map<String, () -> String?>
    ): Reader = resource.readFrom().toInputStream().reader().let { reader ->
        if (resource.contentType?.match(ContentType.Text.Html) == true) {
            filterHtml(
                reader,
                resolveParameters
            )
        } else {
            reader
        }
    }
}
