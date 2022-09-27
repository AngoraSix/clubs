package com.angorasix.clubs.presentation.router

import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.presentation.handler.ClubHandler
import com.angorasix.commons.presentation.filter.checkRequestingContributor
import com.angorasix.commons.presentation.filter.extractRequestingContributor
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.function.server.coRouter

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ClubRouter(
    private val handler: ClubHandler,
    private val objectMapper: ObjectMapper,
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
                    apiConfigs.headers.contributor,
                    objectMapper,
                )
            }
            apiConfigs.basePaths.wellKnown.nest {
                path(apiConfigs.routes.wellKnownPatch.path).nest {
                    filter { request, next ->
                        checkRequestingContributor(
                            request,
                            next,
                            apiConfigs.headers.contributor,
                        )
                    }
                    method(apiConfigs.routes.wellKnownPatch.method, handler::patchWellKnownClub)
                }
                path(apiConfigs.routes.wellKnownGetSingle.path).nest {
                    method(apiConfigs.routes.wellKnownGetSingle.method, handler::getWellKnownClub)
                }
            }
        }
    }
}
