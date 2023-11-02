package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.AdminContributorRequirements
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
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
suspend fun Flow<ClubDto>.generateCollectionModel(): Pair<Boolean, CollectionModel<ClubDto>> {
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
