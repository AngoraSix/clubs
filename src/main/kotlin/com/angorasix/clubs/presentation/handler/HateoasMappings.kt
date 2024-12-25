package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.AdminContributorRequirements
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.infrastructure.token.InvitationTokenInputRequirement
import com.angorasix.clubs.presentation.dto.ClubDto
import com.angorasix.commons.domain.SimpleContributor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.hateoas.server.core.EmbeddedWrapper
import org.springframework.hateoas.server.core.EmbeddedWrappers
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.util.UriComponentsBuilder

/**
 * <p> Class containing all Hateoas Mapping Extensions.</p>
 *
 * @author rozagerardo
 */
fun ClubDto.resolveHypermedia(
    requestingContributor: SimpleContributor?,
    club: Club,
    apiConfigs: ApiConfigs,
    wellKnownClubConfigurations: WellKnownClubConfigurations,
    request: ServerRequest,
): ClubDto {
    val member = requestingContributor?.convertToMember()
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

    // can invite
    if (!club.open && club.isAdmin(requestingContributor?.contributorId)) {
        // send invitation action
        // then modify member, now can be "pending"/"invited"
        // and of course, when pending, it should contain a token to check the invitation
        val inviteContributorRoute = apiConfigs.routes.wellKnownPatch
        val inviteContributorActionName = apiConfigs.clubActions.inviteContributor
        val inviteContributorLink = Link.of(
            uriBuilder(request).path(inviteContributorRoute.resolvePath()).build()
                .toUriString(),
        ).withTitle(inviteContributorActionName).withName(inviteContributorActionName)
            .withRel(inviteContributorActionName).expand(club.id)
        val inviteContributorAffordanceLink =
            Affordances.of(inviteContributorLink).afford(inviteContributorRoute.method)
                .withInput(InvitationTokenInputRequirement::class.java)
                .withName(inviteContributorActionName).toLink()
        add(inviteContributorAffordanceLink)
    }
    return this
}

suspend fun Flow<ClubDto>.generateCollectionModel(): Pair<Boolean, CollectionModel<ClubDto>> {
    val dtoResources = this.toList(mutableListOf())
    val isEmpty = dtoResources.isEmpty()
    val collectionModel = if (isEmpty) {
        val wrappers = EmbeddedWrappers(false)
        val wrapper: EmbeddedWrapper = wrappers.emptyCollectionOf(ClubDto::class.java)
        CollectionModel.of(listOf(wrapper)) as CollectionModel<ClubDto>
    } else {
        CollectionModel.of(dtoResources).withFallbackType(ClubDto::class.java)
    }
    return Pair(isEmpty, collectionModel)
}

fun CollectionModel<ClubDto>.resolveHypermedia(
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

fun CollectionModel<ClubDto>.resolveHypermedia(
    requestingContributor: SimpleContributor?,
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
    if (requestingContributor != null && requestingContributor.isAdminHint == true) {
        // here goes admin-specific hypermedia
    }
    return this
}

fun uriBuilder(request: ServerRequest) = request.requestPath().contextPath().let {
    UriComponentsBuilder.fromHttpRequest(request.exchange().request).replacePath(it.toString()) //
        .replaceQuery("")
}
