package com.angorasix.clubs.infrastructure.applicationevents

import com.angorasix.clubs.messaging.publisher.MessagePublisher
import com.angorasix.commons.infrastructure.intercommunication.dto.club.UserInvited
import com.angorasix.commons.infrastructure.intercommunication.dto.domainresources.A6InfraClubDto
import org.springframework.context.event.EventListener

class ApplicationEventsListener(
    private val messagePublisher: MessagePublisher,
) {
    @EventListener
    fun handleUpdatedAssets(evt: InvitationCreatedApplicationEvent) =
        evt.invitationToken.contributorId?.let {
            messagePublisher.publishClubInvitation(
                userInvited =
                    UserInvited(
                        email = evt.invitationToken.email,
                        club =
                            A6InfraClubDto(
                                id = it,
                                name = evt.club.name,
                                description = evt.club.description ?: "",
                                projectId = evt.club.projectId,
                            ),
                        token = evt.invitationToken.tokenValue,
                    ),
                requestingContributor = evt.requestingContributor,
            )
        }
}
