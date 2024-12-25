package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.infrastructure.config.token.TokenConfigurations
import com.angorasix.clubs.infrastructure.token.InvitationToken
import com.angorasix.clubs.infrastructure.token.InvitationTokenUtils
import com.angorasix.commons.domain.SimpleContributor
import org.springframework.security.oauth2.jwt.JwtEncoder

class InvitationTokenService(
    private val repository: ClubRepository,
    private val jwtEncoder: JwtEncoder,
    private val tokenConfigurations: TokenConfigurations,
) {

    /**
     * Method to create and send an [InvitationToken].
     *
     */
    suspend fun inviteContributor(
        clubId: String,
        email: String,
        requestingContributor: SimpleContributor,
    ): InvitationToken? {
        val club = repository.findById(clubId)
        return if (club?.isAdmin(requestingContributor.contributorId) == true) {
            val invitationToken = InvitationTokenUtils.createInvitationToken(
                jwtEncoder,
                tokenConfigurations,
                email,
                clubId,
            )
            println("Invitation token: $invitationToken")
            // publish token with RabbitMQ
            return invitationToken
        } else null
    }
}