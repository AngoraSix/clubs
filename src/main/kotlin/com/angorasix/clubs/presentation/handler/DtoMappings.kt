package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.MemberStatusValue
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.presentation.dto.ClubDto
import com.angorasix.clubs.presentation.dto.MemberDto
import com.angorasix.commons.domain.A6Contributor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.core.EmbeddedWrapper
import org.springframework.hateoas.server.core.EmbeddedWrappers
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * <p> Class containing all Dto Mapping Extensions.</p>
 *
 * @author rozagerardo
 */
fun Club.convertToDto(
    contributor: A6Contributor?,
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
        projectManagementId,
        if (showAllData) {
            members.map { it.convertToDto() }.toMutableSet()
        } else {
            members
                .filter { it.contributorId == contributor?.contributorId }
                .map { it.convertToDto() }
                .toMutableSet()
        },
        resolveAdmins(contributor),
        if (showAllData) open else null,
        if (showAllData) public else null,
        if (showAllData) social else null,
        if (showAllData) createdInstant else null,
    ).resolveHypermedia(
        contributor,
        this,
        apiConfigs,
        wellKnownClubConfigurations,
        request,
    )
}

suspend fun Flow<ClubDto>.convertToDto(
    contributor: A6Contributor?,
    projectId: String?,
    projectManagementId: String?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): CollectionModel<ClubDto> {
    // Fix this when Spring HATEOAS provides consistent support for reactive/coroutines
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
    return projectManagementId?.let {
        collectionModel.resolveHypermediaForManagement(
            contributor,
            projectManagementId,
            apiConfigs,
            request,
            isEmpty,
        )
    } ?: collectionModel.resolveHypermediaForProject(
        contributor,
        projectId,
        apiConfigs,
        request,
        isEmpty,
    )
}

suspend fun Flow<ClubDto>.convertToDto(
    contributor: A6Contributor?,
    filter: ListClubsFilter,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): CollectionModel<ClubDto> {
    // Fix this when Spring HATEOAS provides consistent support for reactive/coroutines
    val pair = generateCollectionModel()
    return pair.second.resolveHypermedia(
        contributor,
        filter,
        apiConfigs,
        request,
    )
}

fun A6Contributor.convertToMember(status: MemberStatusValue? = null): Member =
    Member(
        contributorId = contributorId,
        emptyList(),
        emptyMap(),
        emptyMap(),
        status ?: MemberStatusValue.INACTIVE,
    )

fun Member.convertToDto(): MemberDto = MemberDto(contributorId, roles, data, status.value)
