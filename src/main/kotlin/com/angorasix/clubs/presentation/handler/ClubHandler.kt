package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.infrastructure.config.ServiceConfigs
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.presentation.dto.ClubDto
import com.angorasix.clubs.presentation.dto.MemberDto
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

/**
 * Club Handler (Controller) containing all handler functions related to Club endpoints.
 *
 * @author rozagerardo
 */
class ClubHandler(
        private val service: ClubService,
        private val serviceConfigs: ServiceConfigs,
) {


    /**
     * Handler for the List Clubs endpoint, retrieving a Flux including all persisted Clubs.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun listClubs(
            @Suppress("UNUSED_PARAMETER") request: ServerRequest
    ): ServerResponse {
        return service.findClubs(request.queryParams().toQueryFilter())
                .map { it.convertToDto() }
                .let {
                    ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                            .bodyAndAwait(it)
                }
    }

    /**
     * Handler for the Get Single Club endpoint, retrieving a Mono with the requested Club.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getWellKnownClub(request: ServerRequest): ServerResponse {
        val projectId = request.pathVariable("projectId")
        val type = request.pathVariable("type")
        return service.findWellKnownClub(type, projectId)
                ?.let {
                    val outputClub = it.convertToDto()
                    ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                            .bodyValueAndAwait(outputClub)
                } ?: ServerResponse.notFound()
                .buildAndAwait()
    }

    /**
     * Handler for the Create Clubs endpoint, to create a new Club entity.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun addMemberToWellKnownClub(request: ServerRequest): ServerResponse {
        val memberDetails = request.attributes()[serviceConfigs.api.contributorHeader]
        val projectId = request.pathVariable("projectId")
        val type = request.pathVariable("type")
        return if (memberDetails is Member) {
            return service.addMemberToWellKnownClub(memberDetails, type, projectId)
                    ?.convertToDto()
                    ?.let {
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                .bodyValueAndAwait(
                                        it
                                )
                    } ?: ServerResponse.notFound().buildAndAwait()

        } else {
            ServerResponse.badRequest().buildAndAwait()
        }
    }
}

private fun Club.convertToDto(): ClubDto {
    return ClubDto(
            id,
            name,
            type,
            description,
            projectId,
            members.map { it.convertToDto() }
                    .toMutableSet(),
            open,
            public,
            social,
            requirements,
            createdAt
    )
}

private fun Member.convertToDto(): MemberDto {
    return MemberDto(
            contributorId,
            roles
    )
}

private fun MultiValueMap<String, String>.toQueryFilter(): ListClubsFilter {
    return ListClubsFilter(getFirst("projectId"),
            getFirst("type"),
            getFirst("contributorId"))
}
