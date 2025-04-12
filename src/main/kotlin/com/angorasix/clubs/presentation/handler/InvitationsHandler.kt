package com.angorasix.clubs.presentation.handler

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.application.InvitationTokenService
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.presentation.dto.InvitationTokenInput
import com.angorasix.commons.domain.DetailedContributor
import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.reactive.presentation.error.resolveBadRequest
import com.angorasix.commons.reactive.presentation.error.resolveExceptionResponse
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
import org.springframework.hateoas.MediaTypes
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

/**
 * Club Handler (Controller) containing all handler functions related to Club endpoints.
 *
 * @author rozagerardo
 */
class InvitationsHandler(
    private val service: ClubService,
    private val invitationTokenService: InvitationTokenService,
    private val apiConfigs: ApiConfigs,
    private val wellKnownClubConfigurations: WellKnownClubConfigurations,
) {
    /**
     * Handler for the Invite Contributor to join a Club endpoint,
     * retrieving an empty 200 OK response if processed correctly.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun inviteContributor(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        val clubId = request.pathVariable("id")
        val tokenInput = request.awaitBody(InvitationTokenInput::class)
        return if (requestingContributor is DetailedContributor) {
            try {
                invitationTokenService
                    .inviteContributor(
                        clubId = clubId,
                        email = tokenInput.email,
                        requestingContributor = requestingContributor,
                        contributorId = null, // tech debt: Trello-93G4IaSy
                    )?.let { ServerResponse.ok().buildAndAwait() }
                    ?: resolveNotFound("Can't invite to this Club", "Club Invitation")
            } catch (ex: RuntimeException) {
                return resolveExceptionResponse(ex, "Club Invitation")
            }
        } else {
            resolveBadRequest("Invalid Contributor", "Contributor")
        }
    }

    /**
     * Handler for the Invite Contributor to join a Club endpoint,
     * retrieving an empty 200 OK response if processed correctly.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun addMemberFromInvitationToken(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        val clubId = request.pathVariable("id")
        val tokenValue = request.pathVariable("tokenValue")
        return if (requestingContributor is SimpleContributor) {
            try {
                service
                    .addMemberFromInvitationToken(
                        tokenValue = tokenValue,
                        clubId = clubId,
                        requestingContributor = requestingContributor,
                    )?.convertToDto(
                        requestingContributor,
                        apiConfigs,
                        wellKnownClubConfigurations,
                        request,
                    )?.let {
                        ServerResponse
                            .ok()
                            .contentType(MediaTypes.HAL_FORMS_JSON)
                            .bodyValueAndAwait(it)
                    }
                    ?: resolveBadRequest("Error with invitation", "Club Invitation")
            } catch (ex: RuntimeException) {
                return resolveExceptionResponse(ex, "Club Invitation")
            }
        } else {
            resolveBadRequest("Invalid Contributor", "Contributor")
        }
    }
}
