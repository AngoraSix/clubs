package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.infrastructure.applicationevents.InvitationCreatedApplicationEvent
import com.angorasix.clubs.infrastructure.config.token.TokenConfigurations
import com.angorasix.clubs.infrastructure.token.InvitationToken
import com.angorasix.clubs.infrastructure.token.InvitationTokenUtils
import com.angorasix.commons.domain.A6Contributor
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder

class InvitationTokenService(
    private val repository: ClubRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val tokenConfigurations: TokenConfigurations,
    private val invitationTokenJwtEncoder: JwtEncoder,
    private val invitationTokenJwtDecoder: JwtDecoder,
) {
    /**
     * Method to create and send an [InvitationToken].
     *
     */
    suspend fun inviteContributor(
        clubId: String,
        email: String,
        requestingContributor: A6Contributor,
        contributorId: String? = null,
    ): InvitationToken? {
        val club = repository.findById(clubId)
        return if (club != null && club.isAdmin(requestingContributor.contributorId)) {
            val invitationToken =
                InvitationTokenUtils.createInvitationToken(
                    jwtEncoder = invitationTokenJwtEncoder,
                    tokenConfigurations = tokenConfigurations,
                    email = email,
                    clubId = clubId,
                    contributorId = contributorId,
                )
            applicationEventPublisher.publishEvent(
                InvitationCreatedApplicationEvent(
                    invitationToken = invitationToken,
                    club = club,
                    requestingContributor = requestingContributor,
                ),
            )
            return invitationToken
        } else {
            null
        }
    }

    /**
     * Method to create and send an [InvitationToken].
     *
     */
    fun checkInvitationToken(tokenValue: String): InvitationToken? =
        InvitationTokenUtils.decodeToken(
            tokenValue = tokenValue,
            jwtDecoder = invitationTokenJwtDecoder,
        )
}
