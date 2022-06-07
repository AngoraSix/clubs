package com.angorasix.clubs.presentation.router

import com.angorasix.clubs.infrastructure.config.ServiceConfigs
import com.angorasix.clubs.presentation.filter.headerFilterFunction
import com.angorasix.clubs.presentation.handler.ClubHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ClubRouter(private val handler: ClubHandler,
                 private val objectMapper: ObjectMapper,
                 private val serviceConfigs: ServiceConfigs) {

    /**
     * Main RouterFunction configuration for all endpoints related to Clubs.
     *
     * @return the [RouterFunction] with all the routes for Clubs
     */
    fun clubRouterFunction() = coRouter {
        "/clubs".nest {
            method(HttpMethod.POST).nest {
                filter { request, next ->
                    headerFilterFunction(request, next, serviceConfigs, objectMapper)
                }
                POST("{id}/add-member",
                        handler::addMember
                )
            }
            accept(APPLICATION_JSON).nest {
                GET(
                        "/{id}",
                        handler::getClub
                )
                GET(
                        "",
                        handler::listClubs
                )
            }
        }

    }
}