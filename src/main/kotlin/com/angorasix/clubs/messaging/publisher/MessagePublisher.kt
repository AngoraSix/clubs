package com.angorasix.clubs.messaging.publisher

import com.angorasix.clubs.infrastructure.config.amqp.AmqpConfigurations
import com.angorasix.clubs.infrastructure.token.InvitationToken
import com.angorasix.commons.domain.DetailedContributor
import com.angorasix.commons.infrastructure.intercommunication.dto.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.dto.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.dto.club.UserInvited
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

class MessagePublisher(
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
) {
    fun publishClubInvitation(
        invitationToken: InvitationToken,
        email: String,
        requestingContributor: DetailedContributor,
        userInvited: UserInvited,
        contributorId: String? = null,
    ) {
        streamBridge.send(
            amqpConfigs.bindings.clubInvitation,
            MessageBuilder
                .withPayload(
                    A6InfraMessageDto(
                        targetId = contributorId ?: email,
                        targetType = A6DomainResource.Contributor,
                        objectId = invitationToken.clubId,
                        objectType = A6DomainResource.Club.value,
                        topic = A6InfraTopics.CLUB_INVITATION.value,
                        requestingContributor = requestingContributor,
                        messageData = userInvited.toMap(),
                    ),
                ).build(),
        )
    }
}

private fun UserInvited.toMap(): Map<String, Any> =
    mapOf(
        "email" to email,
        "club" to club,
        "token" to token,
    )
