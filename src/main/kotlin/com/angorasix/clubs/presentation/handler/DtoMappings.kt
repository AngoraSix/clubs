package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.presentation.dto.ClubDto
import com.angorasix.clubs.presentation.dto.MemberDto
import com.angorasix.commons.domain.SimpleContributor
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
fun Club.convertToDto(): ClubDto {
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

fun Club.convertToDto(
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

suspend fun Flow<ClubDto>.convertToDto(
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

suspend fun Flow<ClubDto>.convertToDto(
    contributor: SimpleContributor?,
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

fun SimpleContributor.convertToMember(): Member {
    return Member(contributorId, emptyList(), emptyMap())
}

fun Member.convertToDto(): MemberDto {
    return MemberDto(contributorId, roles, data)
}
