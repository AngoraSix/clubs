package com.angorasix.clubs.presentation.filter

import com.angorasix.clubs.infrastructure.config.ApiConfigs
import com.angorasix.commons.domain.RequestingContributor
import com.angorasix.commons.presentation.dto.ContributorHeaderDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait
import java.util.*

/**
 * <p>
 *     Filter functions for Clubs Svc.
 * </p>
 *
 * @author rozagerardo
 */
suspend fun headerFilterFunction(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse,
        apiConfigs: ApiConfigs,
        objectMapper: ObjectMapper,
        anonymousRequestAllowed: Boolean = false
): ServerResponse {
    request.headers().header(apiConfigs.headers.contributor).firstOrNull()?.let {
        val contributorHeaderString = Base64.getUrlDecoder().decode(it)
        val contributorHeader = objectMapper.readValue(contributorHeaderString, ContributorHeaderDto::class.java)
        val requestingContributorToken = RequestingContributor(contributorHeader.contributorId, contributorHeader.projectAdmin)
        request.attributes()[apiConfigs.headers.contributor] = requestingContributorToken
        return next(request)
    }
    return if (anonymousRequestAllowed) next(request) else ServerResponse.status(HttpStatus.UNAUTHORIZED).buildAndAwait();
}