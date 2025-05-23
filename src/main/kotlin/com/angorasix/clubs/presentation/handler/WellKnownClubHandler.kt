package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.presentation.dto.SupportedPatchOperations
import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.presentation.dto.Patch
import com.angorasix.commons.reactive.presentation.error.resolveBadRequest
import com.angorasix.commons.reactive.presentation.error.resolveExceptionResponse
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
import com.angorasix.commons.reactive.presentation.utils.affectedContributors
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.springframework.hateoas.MediaTypes
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * Club Handler (Controller) containing all handler functions related to Club endpoints.
 *
 * @author rozagerardo
 */
class WellKnownClubHandler(
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
        val pathVariables = request.pathVariables()
        val projectId = pathVariables["projectId"]
        val projectManagementId = pathVariables["projectManagementId"]

        return if (requestingContributor is A6Contributor) {
            try {
                service
                    .registerAllWellKnownClub(
                        requestingContributor,
                        projectId,
                        projectManagementId,
                    ).asFlow()
                    .map {
                        it.convertToDto(
                            requestingContributor,
                            apiConfigs,
                            wellKnownClubConfigurations,
                            request,
                        )
                    }.let {
                        ServerResponse
                            .ok()
                            .contentType(MediaTypes.HAL_FORMS_JSON)
                            .bodyValueAndAwait(
                                it.convertToDto(
                                    requestingContributor,
                                    projectId,
                                    projectManagementId,
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
        val pathVariables = request.pathVariables()
        val projectId = pathVariables["projectId"]
        val projectManagementId = pathVariables["projectManagementId"]

        val type = request.pathVariable("type")
        return service.getWellKnownClub(type, projectId, projectManagementId, contributor as A6Contributor?)?.let {
            val outputClub =
                it.convertToDto(
                    contributor,
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
    suspend fun getWellKnownClubs(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY] as? A6Contributor
        val pathVariables = request.pathVariables()
        val projectId = pathVariables["projectId"]
        val projectManagementId = pathVariables["projectManagementId"]

        val queryFilter =
            ListClubsFilter(
                projectId = projectId?.let(::listOf),
                projectManagementId = projectManagementId?.let(::listOf),
                memberContributorId = requestingContributor?.let { listOf(it.contributorId) },
            )
        return service
            .findClubs(
                queryFilter,
                requestingContributor,
            ).map {
                it.convertToDto(
                    requestingContributor,
                    apiConfigs,
                    wellKnownClubConfigurations,
                    request,
                )
            }.let {
                ServerResponse
                    .ok()
                    .contentType(MediaTypes.HAL_FORMS_JSON)
                    .bodyValueAndAwait(
                        it.convertToDto(
                            requestingContributor,
                            projectId,
                            projectManagementId,
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
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY] as? A6Contributor
        val queryFilter = ListClubsFilter.fromMultiValueMap(request.queryParams())
        return service
            .findClubs(
                queryFilter,
                requestingContributor,
            ).map {
                it.convertToDto(
                    requestingContributor,
                    apiConfigs,
                    wellKnownClubConfigurations,
                    request,
                )
            }.let {
                ServerResponse
                    .ok()
                    .contentType(MediaTypes.HAL_FORMS_JSON)
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
        val pathVariables = request.pathVariables()
        val projectId = pathVariables["projectId"]
        val projectManagementId = pathVariables["projectManagementId"]

        val type = request.pathVariable("type")
        val patch = request.awaitBody(Patch::class)
        return if (contributor is A6Contributor) {
            try {
                val modifyOperations =
                    patch.operations.map {
                        it.toDomainObjectModification(
                            contributor,
                            SupportedPatchOperations.values().map { o -> o.op }.toList(),
                            objectMapper,
                        )
                    }
                val modifyClubOperations: List<ClubModification<Any>> =
                    modifyOperations.filterIsInstance<ClubModification<Any>>()
                val serviceOutput =
                    service.modifyWellKnownClub(contributor, type, projectId, projectManagementId, modifyClubOperations)
                val affectedContributorsIds =
                    serviceOutput?.admins?.map { it.contributorId } ?: emptyList()
                serviceOutput
                    ?.convertToDto(
                        contributor,
                        apiConfigs,
                        wellKnownClubConfigurations,
                        request,
                    )?.let {
                        ServerResponse
                            .ok()
                            .affectedContributors(request, affectedContributorsIds)
                            .contentType(MediaTypes.HAL_FORMS_JSON)
                            .bodyValueAndAwait(it)
                    } ?: resolveNotFound("Can't patch this Well-Known Club", "Well-known Club")
            } catch (ex: RuntimeException) {
                return resolveExceptionResponse(ex, "Well-Known Club")
            }
        } else {
            resolveBadRequest("Invalid Contributor", "Contributor")
        }
    }
}
