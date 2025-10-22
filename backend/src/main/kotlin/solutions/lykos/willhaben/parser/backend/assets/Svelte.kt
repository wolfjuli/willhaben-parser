package solutions.lykos.willhaben.parser.backend.assets

import io.ktor.http.*
import io.ktor.http.content.*
import java.io.Reader

object Svelte : AssetLayout() {
    const val baseNameTag = "basename"
    private const val titleTag = "title"
    private const val logo = "logo.png"
    private const val images = "images"
    private const val faviconPath = "/favicon.ico"
    private const val fonts = "fonts"
    private const val frontendResorucePath = "solutions/lykos/willhaben/parser/frontend"

    override val assetsPath = "/assets"
    private val fontsPath = "$assetsPath/$fonts"
    private val imagesPath = "$assetsPath/$images"
    override val profileImagePathPattern = "/$imagesPath/profile/{userId}.png"

    private val logoPath = "$imagesPath/$logo"
    private val themeCssPath = "$assetsPath/theme.css"
    private val rootURLPattern = Regex("(?<=\"rootURL\":\")[^\"]*(?=\")")
    private val htmlParamRegex = Regex("/?\\{([^}]+)}")
    private val paramRegex = Regex("\\$\\{([^}]+)}")

    private val fontsRegex = Regex("$fontsPath/(.+\\.(?:eot|otf|svg|ttf|woff2|woff))")
    private val fontUrlRegex = Regex("url\\(\\.\\./$fonts/(.+?)\\)")
    private val cssRegex = Regex("$assetsPath/(.+\\.css)")

    override fun resolveResource(path: String, themeId: String): Resource? {
        when (path) {
            faviconPath -> {
                val favIcon = this::class.java.getResourceAsStream("$frontendResorucePath$faviconPath")
                    ?.readAllBytes()
                    ?: return null
                return Resource(favIcon, "image/x-icon", false)
            }

            themeCssPath -> {
                val css = this::class.java.getResourceAsStream("$frontendResorucePath/$themeId.css")
                    ?.readAllBytes()
                    ?: return null
                return Resource(css, "text/css", false)
            }

            else -> {
                val stream = this::class.java.getResourceAsStream("$frontendResorucePath/$path") ?: return null
                val contentType = ContentType.defaultForFilePath(path)

                return Resource(stream.readAllBytes(), contentType.toString(), false)
            }
        }
    }

    override fun filterResource(
        resource: OutgoingContent.ReadChannelContent,
        resolveParameters: Map<String, () -> String?>
    ): Reader {
        val reader = super.filterResource(resource, resolveParameters)
        return if (resource.contentType?.match(ContentType.Text.JavaScript) == true
            || resource.contentType?.match(ContentType.Application.JavaScript) == true
        )
            filterHtml(reader, resolveParameters)
        else
            reader
    }

    override fun filterHtml(
        content: Reader,
        resolveParameters: Map<String, () -> String?>
    ): Reader {
        return content.buffered().useLines { lines ->
            lines.joinToString("\n") { line ->
                htmlParamRegex.replace(line) { matchResult ->
                    val (paramName) = matchResult.destructured
                    when (paramName) {
                        titleTag ->
                            resolveParameters[paramName]?.invoke() ?: "WillHaben Parser"

                        else ->
                            resolveParameters[paramName]?.invoke()
                                ?: matchResult.value
                    }


                }
            }
        }.reader()
    }
}
