package com.angorasix.clubs.messaging.publisher

import com.angorasix.clubs.infrastructure.config.amqp.AmqpConfigurations
import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.intercommunication.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.club.ClubMemberJoined
import com.angorasix.commons.infrastructure.intercommunication.club.UserInvited
import com.angorasix.commons.infrastructure.intercommunication.messaging.A6InfraMessageDto
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder

class MessagePublisher(
    private val streamBridge: StreamBridge,
    private val amqpConfigs: AmqpConfigurations,
) {
    fun publishClubInvitation(
        userInvited: UserInvited,
        contributorId: String? = null,
        requestingContributor: A6Contributor,
    ) {
        streamBridge.send(
            amqpConfigs.bindings.clubInvitation,
            MessageBuilder
                .withPayload(
                    A6InfraMessageDto(
                        targetId = contributorId ?: userInvited.email,
                        targetType = A6DomainResource.CONTRIBUTOR,
                        objectId = userInvited.club.id,
                        objectType = A6DomainResource.CLUB.value,
                        topic = A6InfraTopics.CLUB_INVITATION.value,
                        requestingContributor = requestingContributor,
                        messageData = userInvited,
                    ),
                ).build(),
        )
    }

    fun publishMemberJoined(
        memberJoined: ClubMemberJoined,
        requestingContributor: A6Contributor,
    ) {
        memberJoined.club.projectId?.let {
            streamBridge.send(
                amqpConfigs.bindings.projectClubMemberJoined,
                MessageBuilder
                    .withPayload(
                        A6InfraMessageDto(
                            targetId = it,
                            targetType = A6DomainResource.PROJECT,
                            objectId = memberJoined.club.id,
                            objectType = A6DomainResource.CLUB.value,
                            topic = A6InfraTopics.PROJECT_CLUB_MEMBER_JOINED.value,
                            requestingContributor = requestingContributor,
                            messageData = memberJoined,
                        ),
                    ).build(),
            )
        }
        memberJoined.club.managementId?.let {
            streamBridge.send(
                amqpConfigs.bindings.managementClubMemberJoined,
                MessageBuilder
                    .withPayload(
                        A6InfraMessageDto(
                            targetId = it,
                            targetType = A6DomainResource.PROJECT_MANAGEMENT,
                            objectId = memberJoined.club.id,
                            objectType = A6DomainResource.CLUB.value,
                            topic = A6InfraTopics.MANAGEMENT_CLUB_MEMBER_JOINED.value,
                            requestingContributor = requestingContributor,
                            messageData = memberJoined,
                        ),
                    ).build(),
            )
        }
    }
}
