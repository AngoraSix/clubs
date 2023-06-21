package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.presentation.dto.ClubDto
import com.angorasix.clubs.presentation.dto.MemberDto
import com.angorasix.clubs.presentation.dto.SupportedPatchOperations
import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.presentation.dto.Patch
import com.angorasix.commons.reactive.presentation.error.resolveBadRequest
import com.angorasix.commons.reactive.presentation.error.resolveExceptionResponse
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.map
import org.springframework.hateoas.Link
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.http.HttpMethod
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.util.UriComponentsBuilder

/**
 * Club Handler (Controller) containing all handler functions related to Club endpoints.
 *
 * @author rozagerardo
 */
class ClubHandler(
    private val service: ClubService,
    private val apiConfigs: ApiConfigs,
    private val wellKnownClubConfigurations: WellKnownClubConfigurations,
    private val objectMapper: ObjectMapper,
) {

    /**
     * Handler for the Patch Club endpoint, retrieving a Mono with the requested Club.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun registerWellKnownClubs(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        val projectId = request.pathVariable("projectId")
        return if (requestingContributor is SimpleContributor) {
            try {
                val wellKnownClubs =
                    service.registerAllWellKnownClub(
                        requestingContributor,
                        projectId,
                    ).map {
                        it?.convertToDto(
                            requestingContributor,
                            apiConfigs,
                            wellKnownClubConfigurations,
                            request,
                        )
                    }
                return ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                    .bodyValueAndAwait(wellKnownClubs)
            } catch (ex: RuntimeException) {
                return resolveExceptionResponse(ex, "Well-Known Club")
            }
        } else {
            resolveBadRequest("Invalid Contributor Authentication", "Contributor Authentication")
        }
    }

    /**
     * Handler for the Get Single Club endpoint, retrieving a Mono with the requested Club.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getWellKnownClub(request: ServerRequest): ServerResponse {
        val contributor = request.attributes()[apiConfigs.headers.contributor]
        val projectId = request.pathVariable("projectId")
        val type = request.pathVariable("type")
        return service.getWellKnownClub(type, projectId)?.let {
            val outputClub = it.convertToDto(
                contributor as? SimpleContributor,
                apiConfigs,
                wellKnownClubConfigurations,
                request,
            )
            ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputClub)
        } ?: resolveNotFound("Well-Known Club not found", "Well-Known Club")
    }

    /**
     * Handler for the Get All Club endpoint with filters (usually by projectId),
     * retrieving a Flux with all the matching Clubs.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getWellKnownClubsAll(request: ServerRequest): ServerResponse {
        val requestingContributor = request.attributes()[apiConfigs.headers.contributor]
        return service.findClubs(
            request.queryParams().toQueryFilter(),
            requestingContributor as? SimpleContributor,
        ).map {
            it.convertToDto(
                requestingContributor as? SimpleContributor,
                apiConfigs,
                wellKnownClubConfigurations,
                request,
            )
        }
            .let {
                ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyAndAwait(it)
            }
    }

    /**
     * Handler for the Patch Club endpoint, retrieving a Mono with the requested Club.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun patchWellKnownClub(request: ServerRequest): ServerResponse {
        val contributor = request.attributes()[apiConfigs.headers.contributor]
        val projectId = request.pathVariable("projectId")
        val type = request.pathVariable("type")
        val patch = request.awaitBody(Patch::class)
        return if (contributor is SimpleContributor) {
            try {
                val modifyOperations = patch.operations.map {
                    it.toDomainObjectModification(
                        contributor,
                        SupportedPatchOperations.values().map { it.op }.toList(),
                        objectMapper,
                    )
                }
                val modifyClubOperations: List<ClubModification<Any>> =
                    modifyOperations.filterIsInstance<ClubModification<Any>>()
                service.modifyWellKnownClub(contributor, type, projectId, modifyClubOperations)
                    ?.convertToDto(contributor, apiConfigs, wellKnownClubConfigurations, request)
                    ?.let {
                        ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                            .bodyValueAndAwait(it)
                    } ?: resolveNotFound("Can't patch this Well-Known Club", "Well-known Club")
            } catch (ex: RuntimeException) {
                return resolveExceptionResponse(ex, "Well-Known Club")
            }
        } else {
            resolveBadRequest("Invalid Contributor Header", "Contributor Header")
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
        members.map { it.convertToDto() }.toMutableSet(),
        open,
        public,
        social,
        createdAt,
    )
}

private fun Club.convertToDto(
    contributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    wellKnownClubConfigurations: WellKnownClubConfigurations,
    request: ServerRequest,
): ClubDto {
    val showAllData = isVisibleToContributor(contributor)
    return ClubDto(
        id,
        name,
        type,
        description,
        projectId,
        if (showAllData) members.map { it.convertToDto() }.toMutableSet()
        else members.filter { it.contributorId == contributor?.id }.map { it.convertToDto() }
            .toMutableSet(),
        if (showAllData) open else null,
        if (showAllData) public else null,
        if (showAllData) social else null,
        if (showAllData) createdAt else null,
    )
        .resolveHypermedia(
            contributor?.convertToMember(),
            this,
            apiConfigs,
            wellKnownClubConfigurations,
            request,
        )
}

private fun ClubDto.resolveHypermedia(
    member: Member?,
    club: Club,
    apiConfigs: ApiConfigs,
    wellKnownClubConfigurations: WellKnownClubConfigurations,
    request: ServerRequest,
): ClubDto {
    val wellKnownGetSingleRoute = apiConfigs.routes.wellKnownGetSingle
    // self
    val selfLink = Link.of(
        uriBuilder(request).path(wellKnownGetSingleRoute.resolvePath()).build().toUriString(),
    ).withRel(wellKnownGetSingleRoute.name).expand(projectId, type).withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)

    // add member
    if (member != null) {
        if (club.canAddMember(member)) {
            val wellKnownAddMemberRoute = apiConfigs.routes.wellKnownPatch
            val wellKnownAddMemberActionName = apiConfigs.clubActions.addMember
            val addMemberLink = Link.of(
                uriBuilder(request).path(wellKnownAddMemberRoute.resolvePath()).build()
                    .toUriString(),
            ).withTitle(wellKnownAddMemberActionName).withName(wellKnownAddMemberActionName)
                .withRel(wellKnownAddMemberActionName).expand(projectId, type)
            val addMemberAffordanceLink =
                Affordances.of(addMemberLink).afford(wellKnownAddMemberRoute.method)
                    .withInput(
                        wellKnownClubConfigurations.clubs.wellKnownClubDescriptions[type]?.requirements
                            ?: Void::class.java,
                    ).withName(wellKnownAddMemberActionName).toLink()
            add(addMemberAffordanceLink)
        } else if (club.canRemoveMember(member)) {
            val wellKnownRemoveMemberRoute = apiConfigs.routes.wellKnownPatch
            val wellKnownRemoveMemberActionName = apiConfigs.clubActions.removeMember
            val removeMemberLink = Link.of(
                uriBuilder(request).path(wellKnownRemoveMemberRoute.resolvePath()).build()
                    .toUriString(),
            ).withTitle(wellKnownRemoveMemberActionName).withName(wellKnownRemoveMemberActionName)
                .withRel(wellKnownRemoveMemberActionName).expand(projectId, type)
            val removeMemberAffordanceLink =
                Affordances.of(removeMemberLink).afford(wellKnownRemoveMemberRoute.method)
                    .withName(wellKnownRemoveMemberActionName).toLink()
            add(removeMemberAffordanceLink)
        }
    }
    return this
}

private fun uriBuilder(request: ServerRequest) = request.requestPath().contextPath().let {
    UriComponentsBuilder.fromHttpRequest(request.exchange().request).replacePath(it.toString()) //
        .replaceQuery("")
}

private fun SimpleContributor.convertToMember(): Member {
    return Member(id, emptyList(), emptyMap())
}

private fun Member.convertToDto(): MemberDto {
    return MemberDto(contributorId, roles, data)
}

private fun MultiValueMap<String, String>.toQueryFilter(): ListClubsFilter {
    return ListClubsFilter(
        getFirst("projectId")?.split(","),
        getFirst("type"),
        getFirst("contributorId"),
    )
}
