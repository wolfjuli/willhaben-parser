package solutions.lykos.willhaben.parser.backend.assets

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import solutions.lykos.willhaben.parser.backend.routing.*
import io.ktor.server.application.Application as KtorApplication

/**
 * @author Clemens Meinhart
 */
class AssetProvider(configuration: Configuration) {
    private val assetLayout = configuration.layout
    private val resourcePathResolver = configuration.resourcePathResolver
    private val withRootRoute = configuration.withRootRoute

    class Configuration {
        lateinit var layout: AssetLayout
        internal lateinit var resourcePathResolver: (ApplicationCall) -> String
        fun resourcePath(block: (ApplicationCall) -> String) {
            resourcePathResolver = block
        }

        var withRootRoute: Boolean = true
    }

    // Implements ApplicationFeature as a companion object.
    companion object Feature : BaseApplicationPlugin<KtorApplication, Configuration, AssetProvider> {
        // Creates a unique key for the feature.
        override val key = AttributeKey<AssetProvider>(ASSET_PROVIDER_KEY_ID)

        // Code to execute when installing the feature.
        override fun install(
            pipeline: KtorApplication,
            configure: Configuration.() -> Unit
        ): AssetProvider {
            // It is responsibility of the `install` code to call the `configure` method with the mutable configuration.
            val configuration = Configuration().apply(configure)

            val assetProvider = AssetProvider(configuration)

            // must happen after sessions feature is ready (after Features) but before frontend is requested (before Call)
            val phase = PipelinePhase("AssetProvider")
            pipeline.insertPhaseBefore(ApplicationCallPipeline.Call, phase)
            pipeline.intercept(phase) {
                call.attributes.put(key, assetProvider)
            }
            pipeline.routing {
                assetProvider.setupRoutes(this)
            }

            return assetProvider
        }
    }


    private fun setupRoutes(routing: Routing) {
        val assetProvider = this
        with(routing) {
            route(INDEX_FILE_NAME) {
                onApplicationPort {
                    get {
                        serveIndex(this)
                    }
                }
            }

            route(FAVICON_FILE_NAME) {
                get {
                    if (serveBrandingResource(call)) {
                        return@get
                    }
                }
            }

            setOf(
                assetLayout.profileImagePathPattern,
                assetLayout.legacyProfileImagePathPattern
            ).forEach { profileImagePathPattern ->
                route(profileImagePathPattern) {
                    onApplicationPort {
                        get {
                            val userId = call.parameters.getOrFail(USER_ID)
                            val profileImage: ByteArray? = null
                            if (profileImage != null) {
                                call.response.cacheControl(CacheControl.NoCache(null))
                                call.respondBytes(profileImage, ContentType.Image.PNG)
                            } else {
                                call.respond(HttpStatusCode.NotFound)
                            }
                            return@get
                        }
                    }
                }
            }

            setOf(
                assetLayout.assetsPath,
                assetLayout.legacyAssetPath
            ).forEach { assetPath ->
                route("$assetPath/{...}") {
                    onApplicationPort {
                        get {
                            val resourcePackage = resourcePathResolver(call)
                            if (serveBrandingResource(call)) {
                                return@get
                            }

                            val resource =
                                call.resolveResource(
                                    call.request.path(),
                                    resourcePackage,
                                    application.environment.classLoader
                                ) ?: call.resolveResource(
                                    INDEX_FILE_NAME,
                                    resourcePackage,
                                    application.environment.classLoader
                                ) ?: return@get
                            withContext(Dispatchers.IO) {
                                call.response.cacheControl(CacheControl.NoCache(null))
                                val contentReader = assetLayout.filterResource(resource, emptyMap())
                                call.respondBytes(contentReader.readText().toByteArray(), resource.contentType)
                                return@withContext
                            }
                        }
                    }
                }
            }

            if (withRootRoute) {
                route("/") {
                    onApplicationPort {
                        get {
                            serveIndex(this)
                        }
                    }
                }
                route("/{static-content-path-parameter...}") {
                    onApplicationPort {
                        get {
                            // GET route is required for the interceptor to work
                        }
                        intercept(ApplicationCallPipeline.Fallback) {
                            if (call.request.httpMethod != HttpMethod.Get) {
                                return@intercept
                            }

                            val resourcePackage = resourcePathResolver(call)
                            withContext(Dispatchers.IO) {
                                val relativePath = call.request.path()
                                var content: OutgoingContent? = relativePath.let {
                                    call.resolveResource(
                                        relativePath,
                                        resourcePackage,
                                        application.environment.classLoader
                                    )
                                }?.takeIf { it !is JarFileContent || (it.contentLength ?: 0) > 0 }
                                    ?: call.resolveResource(
                                        INDEX_FILE_NAME,
                                        resourcePackage,
                                        application.environment.classLoader
                                    )

                                if (content != null) {
                                    content = assetProvider.filterResource(call, content)

                                    call.response.cacheControl(CacheControl.NoCache(null))
                                    call.respond(content)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun serveIndex(context: RoutingContext) {
        val resourcePackage = resourcePathResolver(context.call)

        val index = context.call.resolveResource(
            INDEX_FILE_NAME,
            resourcePackage,
            context.call.application.environment.classLoader
        )
        withContext(Dispatchers.IO) {
            val params = mutableMapOf(
                BASENAME_FIELD to { "" }
            )
            val contentReader = index?.let {
                assetLayout.filterHtml(
                    index.readFrom().toInputStream().reader(),
                    params
                )
            } ?: return@withContext

            if (index.contentType?.match(ContentType.Text.Html) == true) {
                context.call.response.cacheControl(CacheControl.NoCache(null))
            }
            context.call.respondBytes(contentReader.readText().toByteArray(), index.contentType)
        }
    }

    private suspend fun serveBrandingResource(call: ApplicationCall): Boolean {
        val resource = assetLayout.resolveResource(call.request.path(), call.themeId)
        if (resource != null) {
            if (!resource.cache) {
                call.response.cacheControl(CacheControl.NoCache(null))
            }
            call.respondBytes(resource.data, ContentType.parse(resource.contentType), HttpStatusCode.OK)
            return true
        }
        return false
    }


    private fun filterResource(
        call: ApplicationCall,
        resource: OutgoingContent
    ): OutgoingContent {
        val params = mutableMapOf(
            BASENAME_FIELD to call.basenameProvider
        )

        return if (
            resource.contentType?.match(ContentType.Application.JavaScript) == true
            || resource.contentType?.match(ContentType.Application.Json) == true
            || resource.contentType?.match(ContentType.Text.Any) == true
        )
            ByteArrayContent(
                assetLayout.filterResource(
                    (resource as OutgoingContent.ReadChannelContent),
                    params
                ).readText().toByteArray(),
                resource.contentType,
                resource.status
            )
        else
            resource
    }

    private val ApplicationCall.basenameProvider: () -> String
        get() = { "" }
}
