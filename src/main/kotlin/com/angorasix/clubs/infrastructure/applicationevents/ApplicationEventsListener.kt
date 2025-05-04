package com.angorasix.clubs.infrastructure.applicationevents

import com.angorasix.clubs.messaging.publisher.MessagePublisher
import com.angorasix.commons.infrastructure.intercommunication.club.ClubMemberJoined
import com.angorasix.commons.infrastructure.intercommunication.club.UserInvited
import org.springframework.context.event.EventListener

class ApplicationEventsListener(
    private val messagePublisher: MessagePublisher,
) {
    @EventListener
    fun handleInvitationCreated(evt: InvitationCreatedApplicationEvent) =
        evt.club.id?.let {
            messagePublisher.publishClubInvitation(
                userInvited =
                    UserInvited(
                        email = evt.invitationToken.email,
                        club =
                            UserInvited.ClubDetails(
                                id = it,
                                clubType = evt.club.type,
                                name = evt.club.name,
                                description = evt.club.description ?: "",
                                projectId = evt.club.projectId,
                                managementId = evt.club.projectManagementId,
                            ),
                        token = evt.invitationToken.tokenValue,
                    ),
                requestingContributor = evt.requestingContributor,
            )
        }

    @EventListener
    fun handleMemberJoined(evt: MemberJoinedApplicationEvent) =
        evt.club.id?.let {
            messagePublisher.publishMemberJoined(
                ClubMemberJoined(
                    joinedMemberContributorId = evt.memberContributorId,
                    club =
                        ClubMemberJoined.ClubDetails(
                            id = it,
                            projectId = evt.club.projectId,
                            managementId = evt.club.projectManagementId,
                            clubType = evt.club.type,
                        ),
                ),
                evt.requestingContributor,
            )
        }
}
