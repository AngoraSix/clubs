package com.angorasix.clubs.presentation.filter

import com.angorasix.clubs.infrastructure.config.ServiceConfigs
import com.angorasix.clubs.presentation.dto.ContributorHeaderDto
import com.angorasix.clubs.domain.club.Member
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
        serviceConfigs: ServiceConfigs,
        objectMapper: ObjectMapper
): ServerResponse {
    request.headers().header(serviceConfigs.api.contributorHeader).firstOrNull()?.let {
        val contributorHeaderString = Base64.getUrlDecoder().decode(it)
        val contributorHeader = objectMapper.readValue(contributorHeaderString, ContributorHeaderDto::class.java)
        val contributorToken = Member(contributorHeader.contributorId)
        request.attributes()[serviceConfigs.api.contributorHeader] = contributorToken
        return next(request)
    }
    return ServerResponse.status(HttpStatus.UNAUTHORIZED).buildAndAwait();
}