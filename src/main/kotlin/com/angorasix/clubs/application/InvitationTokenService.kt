package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.infrastructure.config.token.TokenConfigurations
import com.angorasix.clubs.infrastructure.token.InvitationToken
import com.angorasix.clubs.infrastructure.token.InvitationTokenUtils
import com.angorasix.commons.domain.DetailedContributor
import com.angorasix.commons.infrastructure.intercommunication.dto.club.UserInvited
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraClubDto
import messaging.publisher.MessagePublisher
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder

class InvitationTokenService(
    private val repository: ClubRepository,
    private val messagePublisher: MessagePublisher,
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
        requestingContributor: DetailedContributor,
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
            val userInvited =
                UserInvited(
                    email = email,
                    club =
                        A6InfraClubDto(
                            id = clubId,
                            name = club.name,
                            description = club.description ?: "",
                            projectId = club.projectId,
                        ),
                    token = invitationToken.tokenValue,
                )
            messagePublisher.publishClubInvitation(invitationToken, email, requestingContributor, userInvited, contributorId)

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
