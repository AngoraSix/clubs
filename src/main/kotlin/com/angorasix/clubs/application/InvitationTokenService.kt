package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.infrastructure.config.amqp.AmqpConfigurations
import com.angorasix.clubs.infrastructure.config.token.TokenConfigurations
import com.angorasix.clubs.infrastructure.token.InvitationToken
import com.angorasix.clubs.infrastructure.token.InvitationTokenUtils
import com.angorasix.commons.domain.DetailedContributor
import com.angorasix.commons.infrastructure.intercommunication.dto.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.dto.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraClubDto
import com.angorasix.commons.infrastructure.intercommunication.dto.invitations.A6InfraClubInvitation
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.oauth2.jwt.JwtEncoder

class InvitationTokenService(
    private val repository: ClubRepository,
    private val jwtEncoder: JwtEncoder,
    private val tokenConfigurations: TokenConfigurations,
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
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
            val invitationToken = InvitationTokenUtils.createInvitationToken(
                jwtEncoder = jwtEncoder,
                tokenConfigurations = tokenConfigurations,
                email = email,
                clubId = clubId,
                contributorId = contributorId,
            )
            println("Invitation token: $invitationToken")
            val messageData = A6InfraClubInvitation(
                email = email,
                club = A6InfraClubDto(
                    id = clubId,
                    name = club.name,
                    description = club.description ?: "",
                    projectId = club.projectId,
                ),
                token = invitationToken.tokenValue,
            )
            streamBridge.send(
                amqpConfigs.bindings.clubInvitation,
                MessageBuilder.withPayload(
                    A6InfraMessageDto(
                        targetId = contributorId ?: email,
                        targetType = A6DomainResource.Contributor,
                        objectId = invitationToken.clubId,
                        objectType = A6DomainResource.Club.value,
                        topic = A6InfraTopics.CLUB_INVITATION.value,
                        requestingContributor = requestingContributor,
                        messageData = messageData.toMap(),
                    ),
                ).build(),
            )
            return invitationToken
        } else null
    }
}

private fun A6InfraClubInvitation.toMap(): Map<String, Any> {
    return mapOf(
        "email" to email,
        "club" to club,
        "token" to token,
    )
}