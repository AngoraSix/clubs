package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.AdminContributorRequirements
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.presentation.dto.ClubDto
import com.angorasix.clubs.presentation.dto.InvitationTokenInput
import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.reactive.presentation.mappings.addLink
import com.angorasix.commons.reactive.presentation.mappings.addSelfLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.core.EmbeddedWrapper
import org.springframework.hateoas.server.core.EmbeddedWrappers
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * <p> Class containing all Hateoas Mapping Extensions.</p>
 *
 * @author rozagerardo
 */
fun ClubDto.resolveHypermedia(
    requestingContributor: A6Contributor?,
    club: Club,
    apiConfigs: ApiConfigs,
    wellKnownClubConfigurations: WellKnownClubConfigurations,
    request: ServerRequest,
): ClubDto {
    projectManagementId?.let { addSelfLink(apiConfigs.routes.wellKnownGetForManagement, request, listOf(it)) }
        ?: addSelfLink(apiConfigs.routes.wellKnownGetForProject, request, listOf(projectId ?: "undefinedProjectId"))

    val member = requestingContributor?.convertToMember()
    // add member
    if (member != null) {
        resolveHypermediaForMember(
            member,
            club,
            apiConfigs,
            wellKnownClubConfigurations,
            request,
        )
    }

    // can invite
    if (!club.open && club.isAdmin(requestingContributor?.contributorId) && club.id != null) {
        addLink(
            apiConfigs.routes.inviteContributor,
            apiConfigs.clubActions.inviteContributor,
            request,
            listOf(club.id),
            InvitationTokenInput::class.java,
        )
    }
    return this
}

fun ClubDto.resolveHypermediaForMember(
    member: Member,
    club: Club,
    apiConfigs: ApiConfigs,
    wellKnownClubConfigurations: WellKnownClubConfigurations,
    request: ServerRequest,
) {
    if (club.canAddMember(member)) {
        projectId?.let {
            addLink(
                apiConfigs.routes.wellKnownPatchForProjectAndType,
                apiConfigs.clubActions.addMemberForProject,
                request,
                listOf(it, type),
                wellKnownClubConfigurations.wellKnownClubDescriptions[type]?.requirements ?: Void::class.java,
            )
        }
        projectManagementId?.let {
            addLink(
                apiConfigs.routes.wellKnownPatchForManagementAndType,
                apiConfigs.clubActions.addMemberForManagement,
                request,
                listOf(it, type),
                wellKnownClubConfigurations.wellKnownClubDescriptions[type]?.requirements ?: Void::class.java,
            )
        }
    } else if (club.canRemoveMember(member)) {
        projectId?.let {
            addLink(
                apiConfigs.routes.wellKnownPatchForProjectAndType,
                apiConfigs.clubActions.removeMemberForProject,
                request,
                listOf(it, type),
            )
        }
        projectManagementId?.let {
            addLink(
                apiConfigs.routes.wellKnownPatchForManagementAndType,
                apiConfigs.clubActions.removeMemberForManagement,
                request,
                listOf(it, type),
            )
        }
    }
}

suspend fun Flow<ClubDto>.generateCollectionModel(): Pair<Boolean, CollectionModel<ClubDto>> {
    val dtoResources = this.toList(mutableListOf())
    val isEmpty = dtoResources.isEmpty()
    val collectionModel =
        if (isEmpty) {
            val wrappers = EmbeddedWrappers(false)
            val wrapper: EmbeddedWrapper = wrappers.emptyCollectionOf(ClubDto::class.java)
            CollectionModel.of(listOf(wrapper)) as CollectionModel<ClubDto>
        } else {
            CollectionModel.of(dtoResources).withFallbackType(ClubDto::class.java)
        }
    return Pair(isEmpty, collectionModel)
}

fun CollectionModel<ClubDto>.resolveHypermediaForProject(
    requestingContributor: A6Contributor?,
    projectId: String?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
    isEmpty: Boolean,
): CollectionModel<ClubDto> {
    addSelfLink(apiConfigs.routes.wellKnownGetForProject, request, listOf(projectId ?: "undefinedProjectId"))
    // register wellknown clubs
    if (requestingContributor != null && requestingContributor.isAdminHint == true && isEmpty) {
        projectId?.let {
            addLink(
                apiConfigs.routes.wellKnownRegisterForProject,
                apiConfigs.clubActions.registerAllForProject,
                request,
                listOf(it),
                AdminContributorRequirements::class.java,
            )
        }
    }
    return this
}

fun CollectionModel<ClubDto>.resolveHypermediaForManagement(
    requestingContributor: A6Contributor?,
    projectManagementId: String?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
    isEmpty: Boolean,
): CollectionModel<ClubDto> {
    addSelfLink(apiConfigs.routes.wellKnownGetForManagement, request, listOf(projectManagementId ?: "undefinedProjectManagementId"))
    // register wellknown clubs
    if (requestingContributor != null && requestingContributor.isAdminHint == true && isEmpty) {
        projectManagementId?.let {
            addLink(
                apiConfigs.routes.wellKnownRegisterForManagement,
                apiConfigs.clubActions.registerAllForManagement,
                request,
                listOf(it),
                AdminContributorRequirements::class.java,
            )
        }
    }
    return this
}

fun CollectionModel<ClubDto>.resolveHypermedia(
    requestingContributor: A6Contributor?,
    filter: ListClubsFilter,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): CollectionModel<ClubDto> {
    addSelfLink(apiConfigs.routes.wellKnownSearch, request)
    if (requestingContributor != null && requestingContributor.isAdminHint == true) {
        // here goes admin-specific hypermedia
        println("Admin specific hypermedia for filter $filter")
    }
    return this
}
