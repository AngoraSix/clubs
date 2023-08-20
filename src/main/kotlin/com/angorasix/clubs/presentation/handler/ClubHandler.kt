package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.AdminContributorRequirements
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.hateoas.server.core.EmbeddedWrapper
import org.springframework.hateoas.server.core.EmbeddedWrappers
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
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
     * Handler for the Register all Well-known Club endpoint, retrieving a Flow of the created/registered Clubs.
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
                service.registerAllWellKnownClub(
                    requestingContributor,
                    projectId,
                ).asFlow().map {
                    it.convertToDto(
                        requestingContributor,
                        apiConfigs,
                        wellKnownClubConfigurations,
                        request,
                    )
                }.let {
                    ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                        .bodyValueAndAwait(
                            it.convertToDto(
                                requestingContributor,
                                projectId,
                                apiConfigs,
                                request,
                            ),
                        )
                }
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
        val contributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
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
    suspend fun getWellKnownClubsForProject(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY] as? SimpleContributor
        val projectId = request.pathVariable("projectId")
        val queryFilter = ListClubsFilter(
            listOf(projectId),
            null,
            requestingContributor?.let { listOf(it.contributorId) },
        )
        return service.findClubs(
            queryFilter,
            requestingContributor,
        ).map {
            it.convertToDto(
                requestingContributor,
                apiConfigs,
                wellKnownClubConfigurations,
                request,
            )
        }
            .let {
                ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                    .bodyValueAndAwait(
                        it.convertToDto(
                            requestingContributor,
                            projectId,
                            apiConfigs,
                            request,
                        ),
                    )
            }
    }

    /**
     * Handler for the Search Clubs with filters (usually by projectId),
     * retrieving a Flux with all the matching Clubs.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun searchWellKnownClubs(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY] as? SimpleContributor
        val queryFilter = ListClubsFilter.fromMultiValueMap(request.queryParams())
        return service.findClubs(
            queryFilter,
            requestingContributor,
        ).map {
            it.convertToDto(
                requestingContributor,
                apiConfigs,
                wellKnownClubConfigurations,
                request,
            )
        }
            .let {
                ServerResponse.ok().contentType(MediaTypes.HAL_FORMS_JSON)
                    .bodyValueAndAwait(
                        it.convertToDto(
                            requestingContributor,
                            queryFilter,
                            apiConfigs,
                            request,
                        ),
                    )
            }
    }

    /**
     * Handler for the Patch Club endpoint, retrieving a Mono with the requested Club.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun patchWellKnownClub(request: ServerRequest): ServerResponse {
        val contributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
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
        mutableSetOf(),
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
        if (showAllData) {
            members.map { it.convertToDto() }.toMutableSet()
        } else {
            members.filter { it.contributorId == contributor?.contributorId }
                .map { it.convertToDto() }
                .toMutableSet()
        },
        resolveAdmins(contributor),
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
                        wellKnownClubConfigurations.wellKnownClubDescriptions[type]?.requirements
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

private suspend fun Flow<ClubDto>.convertToDto(
    contributor: SimpleContributor?,
    projectId: String,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): CollectionModel<ClubDto> {
    // Fix this when Spring HATEOAS provides consistent support for reactive/coroutines
    val dtoResources = this.toList(mutableListOf())
    val isEmpty = dtoResources.isNullOrEmpty()
    val collectionModel = if (isEmpty) {
        val wrappers = EmbeddedWrappers(false)
        val wrapper: EmbeddedWrapper = wrappers.emptyCollectionOf(ClubDto::class.java)
        CollectionModel.of(listOf(wrapper)) as CollectionModel<ClubDto>
    } else {
        CollectionModel.of(dtoResources).withFallbackType(ClubDto::class.java)
    }
    return collectionModel.resolveHypermedia(
        contributor,
        projectId,
        apiConfigs,
        request,
        isEmpty,
    )
}

private suspend fun Flow<ClubDto>.convertToDto(
    contributor: SimpleContributor?,
    filter: ListClubsFilter,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): CollectionModel<ClubDto> {
    // Fix this when Spring HATEOAS provides consistent support for reactive/coroutines
    val pair = generateCollectionModel()
    return pair.second.resolveHypermedia(
        filter,
        apiConfigs,
        request,
    )
}

private suspend fun Flow<ClubDto>.generateCollectionModel(): Pair<Boolean, CollectionModel<ClubDto>> {
    val dtoResources = this.toList(mutableListOf())
    val isEmpty = dtoResources.isNullOrEmpty()
    val collectionModel = if (isEmpty) {
        val wrappers = EmbeddedWrappers(false)
        val wrapper: EmbeddedWrapper = wrappers.emptyCollectionOf(ClubDto::class.java)
        CollectionModel.of(listOf(wrapper)) as CollectionModel<ClubDto>
    } else {
        CollectionModel.of(dtoResources).withFallbackType(ClubDto::class.java)
    }
    return Pair(isEmpty, collectionModel)
}


private fun CollectionModel<ClubDto>.resolveHypermedia(
    requestingContributor: SimpleContributor?,
    projectId: String,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
    isEmpty: Boolean,
): CollectionModel<ClubDto> {
    val wellKnownGetAllRoute = apiConfigs.routes.wellKnownGetForProject
    // self
    val selfLink = Link.of(
        uriBuilder(request).path(wellKnownGetAllRoute.resolvePath()).build().toUriString(),
    ).withRel(wellKnownGetAllRoute.name).expand(projectId).withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)
    // register wellknown clubs
    if (requestingContributor != null && requestingContributor.isAdminHint == true && isEmpty) {
        val wellKnownRegisterAllRoute = apiConfigs.routes.wellKnownRegister
        val wellKnownRegisterAllActionName = apiConfigs.clubActions.registerAll
        val registerAllWellknownLink = Link.of(
            uriBuilder(request).path(wellKnownRegisterAllRoute.resolvePath()).build()
                .toUriString(),
        ).withTitle(wellKnownRegisterAllActionName).withName(wellKnownRegisterAllActionName)
            .withRel(wellKnownRegisterAllActionName).expand(projectId)
        val registerAllAffordanceLink =
            Affordances.of(registerAllWellknownLink).afford(wellKnownRegisterAllRoute.method)
                .withInput(AdminContributorRequirements::class.java)
                .withName(wellKnownRegisterAllActionName).toLink()
        add(registerAllAffordanceLink)
    }
    return this
}

private fun CollectionModel<ClubDto>.resolveHypermedia(
    filter: ListClubsFilter,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): CollectionModel<ClubDto> {
    val wellKnownSearchRoute = apiConfigs.routes.wellKnownSearch
    // self
    val selfLink = Link.of(
        uriBuilder(request).path(wellKnownSearchRoute.resolvePath())
            .queryParams(filter.toMultiValueMap()).build()
            .toUriString(),
    ).withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)
    return this
}

private fun uriBuilder(request: ServerRequest) = request.requestPath().contextPath().let {
    UriComponentsBuilder.fromHttpRequest(request.exchange().request).replacePath(it.toString()) //
        .replaceQuery("")
}

private fun SimpleContributor.convertToMember(): Member {
    return Member(contributorId, emptyList(), emptyMap())
}

private fun Member.convertToDto(): MemberDto {
    return MemberDto(contributorId, roles, data)
}
