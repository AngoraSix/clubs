package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.infrastructure.config.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.presentation.error.resolveBadRequest
import com.angorasix.clubs.infrastructure.presentation.error.resolveExceptionResponse
import com.angorasix.clubs.infrastructure.presentation.error.resolveNotFound
import com.angorasix.clubs.infrastructure.presentation.rest.patch.Patch
import com.angorasix.clubs.infrastructure.presentation.rest.patch.PatchOperation
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.presentation.dto.ClubDto
import com.angorasix.clubs.presentation.dto.MemberDto
import com.angorasix.clubs.presentation.dto.PatchOperationSpec
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
class ClubHandler(private val service: ClubService, private val apiConfigs: ApiConfigs, private val wellKnownClubConfigurations: WellKnownClubConfigurations, private val objectMapper: ObjectMapper) {


    /**
     * Handler for the List Clubs endpoint, retrieving a Flux including all persisted Clubs.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun listClubs(@Suppress("UNUSED_PARAMETER") request: ServerRequest): ServerResponse {
        return service.findClubs(request.queryParams().toQueryFilter()).map { it.convertToDto() }.let {
            ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyAndAwait(it)
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
        return service.findWellKnownClubForContributor(contributor as Member?, type, projectId)?.let {
            val outputClub = it.convertToDto(contributor, apiConfigs, wellKnownClubConfigurations, request)
            ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputClub)
        } ?: resolveNotFound("Well-Known Club not found", "Well-Known Club")
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
        return if (contributor is Member) {
            try {
                val modifyOperations = patch.operations.map { it.convertToDomainModification(contributor, objectMapper) }
                service.modifyWellKnownClub(contributor, type, projectId, modifyOperations)?.convertToDto(contributor, apiConfigs, wellKnownClubConfigurations, request)?.let {
                    ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(it)
                } ?: resolveNotFound("Can't patch this Well-Known Club", "Well-known Club")
            } catch (ex: Exception) {
                return resolveExceptionResponse(ex, "Well-Known Club")
            }
        } else {
            resolveBadRequest("Invalid Contributor Header", "Contributor Header")
        }
    }
}

//private suspend fun resolveClubResponse(club: Club, contributor: Member, apiConfigs: ApiConfigs, wellKnownClubConfigurations: WellKnownClubConfigurations, request: ServerRequest ) : ServerResponse {
//    return if (club.isVisibleToMember(contributor)) {
//        val outputClub = club.convertToDto(contributor, apiConfigs, wellKnownClubConfigurations, request)
//        ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
//                .bodyValueAndAwait(outputClub)
//    } else {
//        ServerResponse.noContent().buildAndAwait()
//    }
//}

private fun Club.convertToDto(): ClubDto {
    return ClubDto(id, name, type, description, projectId, members.map { it.convertToDto() }.toMutableSet(), open, public, social, createdAt)
}

private fun Club.convertToDto(contributor: Member?, apiConfigs: ApiConfigs, wellKnownClubConfigurations: WellKnownClubConfigurations, request: ServerRequest): ClubDto {
    val showAllData = isVisibleToMember(contributor)
    return ClubDto(id,
            name,
            type,
            description,
            projectId,
            if (showAllData) members.map { it.convertToDto() }.toMutableSet()
            else members.filter { it.contributorId == contributor?.contributorId }.map { it.convertToDto() }.toMutableSet(),
            if (showAllData) open else null,
            if (showAllData) public else null,
            if (showAllData) social else null,
            if (showAllData) createdAt else null)
            .resolveHypermedia(contributor, this, apiConfigs, wellKnownClubConfigurations, request)
}

private fun ClubDto.resolveHypermedia(contributor: Member?, club: Club, apiConfigs: ApiConfigs, wellKnownClubConfigurations: WellKnownClubConfigurations, request: ServerRequest): ClubDto {
    val wellKnownGetSingleRoute = apiConfigs.routes.wellKnownGetSingle
    // self
    val selfLink = Link.of(uriBuilder(request).path(wellKnownGetSingleRoute.resolvePath()).build().toUriString()).withRel(wellKnownGetSingleRoute.name).expand(projectId, type).withSelfRel()
    val selfLinkWithDefaultAffordance = Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)

    // add member
    if (contributor != null) {
        if (club.canAddMember(contributor)) {
            val wellKnownAddMemberRoute = apiConfigs.routes.wellKnownAddMember
            val addMemberLink = Link.of(uriBuilder(request).path(wellKnownAddMemberRoute.resolvePath()).build().toUriString()).withTitle(wellKnownAddMemberRoute.name).withName(wellKnownAddMemberRoute.name).withRel(wellKnownAddMemberRoute.name).expand(projectId, type)
            val addMemberAffordanceLink = Affordances.of(addMemberLink).afford(HttpMethod.POST).withInput(wellKnownClubConfigurations.clubs.wellKnownClubDescriptions[type]?.requirements ?: Void::class.java).withName(wellKnownAddMemberRoute.name).toLink()
            add(addMemberAffordanceLink)
        } else if (club.canRemoveMember(contributor)) {
            val wellKnownRemoveMemberRoute = apiConfigs.routes.wellKnownRemoveMember
            val removeMemberLink = Link.of(uriBuilder(request).path(wellKnownRemoveMemberRoute.resolvePath()).build().toUriString()).withTitle(wellKnownRemoveMemberRoute.name).withName(wellKnownRemoveMemberRoute.name).withRel(wellKnownRemoveMemberRoute.name).expand(projectId, type)
            val removeMemberAffordanceLink = Affordances.of(removeMemberLink).afford(HttpMethod.POST).withName(wellKnownRemoveMemberRoute.name).toLink()
            add(removeMemberAffordanceLink)
        }
    }
    return this
}

private fun uriBuilder(request: ServerRequest) = request.requestPath().contextPath().let {
    UriComponentsBuilder.fromHttpRequest(request.exchange().request).replacePath(it.toString()) //
            .replaceQuery("")
}

private fun ClubDto.convertToDomain(): Club {
    return Club(id, name ?: throw IllegalArgumentException("Club name expected"), type, description ?: throw IllegalArgumentException("Club description expected"), projectId, members.map { it.convertToModel() }.toMutableSet(), open ?: throw IllegalArgumentException("Club open param expected"), public ?: throw IllegalArgumentException("Club public param expected"), social ?: throw IllegalArgumentException("Club social param expected"), createdAt ?: throw IllegalArgumentException("Club createdAt expected"))
}

private fun PatchOperation.convertToDomainModification(contributor: Member, objectMapper: ObjectMapper): ClubModification<out Any> {
    return PatchOperationSpec.Companion.SupportedOperations.values().find { it.supportsPatchOperation(this) }?.let { it.mapToObjectModification(contributor, this, objectMapper) } ?: throw IllegalArgumentException("Patch Operation not supported")
}

private fun Member.convertToDto(): MemberDto {
    return MemberDto(contributorId, roles)
}

private fun MultiValueMap<String, String>.toQueryFilter(): ListClubsFilter {
    return ListClubsFilter(getFirst("projectId"), getFirst("type"), getFirst("contributorId"))
}

private fun MemberDto.convertToModel(): Member {
    return Member(contributorId ?: throw IllegalArgumentException("Contributor ID expected for Member"), roles)
}
