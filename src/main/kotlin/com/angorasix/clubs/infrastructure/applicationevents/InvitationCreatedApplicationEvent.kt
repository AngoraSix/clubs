package com.angorasix.clubs.infrastructure.applicationevents

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.infrastructure.token.InvitationToken
import com.angorasix.commons.domain.A6Contributor

class InvitationCreatedApplicationEvent(
    val invitationToken: InvitationToken,
    val club: Club,
    val requestingContributor: A6Contributor,
)
