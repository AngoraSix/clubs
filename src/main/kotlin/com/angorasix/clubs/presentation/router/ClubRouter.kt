package com.angorasix.clubs.presentation.router

import com.angorasix.clubs.infrastructure.config.ApiConfigs
import com.angorasix.clubs.presentation.filter.headerFilterFunction
import com.angorasix.clubs.presentation.handler.ClubHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType
import org.springframework.web.reactive.function.server.coRouter

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ClubRouter(private val handler: ClubHandler,
                 private val objectMapper: ObjectMapper,
                 private val apiConfigs: ApiConfigs) {

    /**
     * Main RouterFunction configuration for all endpoints related to Clubs.
     *
     * @return the [RouterFunction] with all the routes for Clubs
     */
    fun clubRouterFunction() = coRouter {
        apiConfigs.basePaths.clubs.nest {
            apiConfigs.basePaths.wellKnown.nest {
                path(apiConfigs.routes.wellKnownAddMember.path).nest {
                    filter { request, next ->
                        headerFilterFunction(request, next, apiConfigs, objectMapper)
                    }
                    method(apiConfigs.routes.wellKnownAddMember.method, handler::addMemberToWellKnownClub)
                }
                path(apiConfigs.routes.wellKnownRemoveMember.path).nest {
                    filter { request, next ->
                        headerFilterFunction(request, next, apiConfigs, objectMapper)
                    }
                    method(apiConfigs.routes.wellKnownRemoveMember.method, handler::removeMemberFromWellKnownClub)
                }
                path(apiConfigs.routes.wellKnownGetSingle.path).nest {
                    filter { request, next ->
                        headerFilterFunction(request, next, apiConfigs, objectMapper, true)
                    }
                    method(apiConfigs.routes.wellKnownGetSingle.method, handler::getWellKnownClub)
                }
            }
        }

    }
}