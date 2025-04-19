package com.angorasix.clubs.messaging.publisher

import com.angorasix.clubs.infrastructure.config.amqp.AmqpConfigurations
import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.intercommunication.dto.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.dto.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.dto.club.UserInvited
import com.angorasix.commons.infrastructure.intercommunication.dto.club.toMap
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

class MessagePublisher(
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
) {
    fun publishClubInvitation(
        requestingContributor: SimpleContributor,
        userInvited: UserInvited,
        contributorId: String? = null,
    ) {
        streamBridge.send(
            amqpConfigs.bindings.clubInvitation,
            MessageBuilder
                .withPayload(
                    A6InfraMessageDto(
                        targetId = contributorId ?: userInvited.email,
                        targetType = A6DomainResource.Contributor,
                        objectId = userInvited.club.id,
                        objectType = A6DomainResource.Club.value,
                        topic = A6InfraTopics.CLUB_INVITATION.value,
                        requestingContributor = requestingContributor,
                        messageData = userInvited.toMap(),
                    ),
                ).build(),
        )
    }
}
