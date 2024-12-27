package com.angorasix.clubs.presentation.router

import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.presentation.handler.ClubHandler
import com.angorasix.commons.reactive.presentation.filter.extractRequestingContributor
import org.springframework.web.reactive.function.server.coRouter

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ClubRouter(
    private val handler: ClubHandler,
    private val apiConfigs: ApiConfigs,
) {

    /**
     * Main RouterFunction configuration for all endpoints related to Clubs.
     *
     * @return the RouterFunction with all the routes for Clubs
     */
    fun clubRouterFunction() = coRouter {
        apiConfigs.basePaths.clubs.nest {
            filter { request, next ->
                extractRequestingContributor(
                    request,
                    next,
                )
            }
            apiConfigs.basePaths.baseWellKnown.nest {
                path(apiConfigs.routes.wellKnownPatch.path).nest {
                    method(apiConfigs.routes.wellKnownPatch.method, handler::patchWellKnownClub)
                }
//                path(apiConfigs.routes.wellKnownGetSingle.path).nest {
//                    method(apiConfigs.routes.wellKnownGetSingle.method, handler::getWellKnownClub)
//                }
                path(apiConfigs.routes.wellKnownGetForProject.path).nest {
                    method(apiConfigs.routes.wellKnownGetForProject.method, handler::getWellKnownClubsForProject)
                }
                path(apiConfigs.routes.wellKnownSearch.path).nest {
                    method(apiConfigs.routes.wellKnownSearch.method, handler::searchWellKnownClubs)
                }
                path(apiConfigs.routes.wellKnownRegister.path).nest {
                    method(apiConfigs.routes.wellKnownRegister.method, handler::registerWellKnownClubs)
                }
            }
            apiConfigs.basePaths.baseByIdRoute.nest {
                path(apiConfigs.routes.inviteContributor.path).nest {
                    method(apiConfigs.routes.inviteContributor.method, handler::inviteContributor)
                }
            }
        }
    }
}
