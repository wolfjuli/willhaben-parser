package solutions.lykos.willhaben.parser.backend.routing

import io.ktor.server.routing.*

object ApplicationRouteSelector : RouteSelector() {
    override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Success(RouteSelectorEvaluation.qualityConstant)
    }
}

fun Route.onApplicationPort(build: Route.() -> Unit): Route {
    return createChild(ApplicationRouteSelector).apply(build)
}
