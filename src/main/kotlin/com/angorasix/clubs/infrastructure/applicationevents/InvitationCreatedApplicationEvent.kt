package com.angorasix.clubs.infrastructure.applicationevents

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.infrastructure.token.InvitationToken
import com.angorasix.commons.domain.SimpleContributor

class InvitationCreatedApplicationEvent(
    val invitationToken: InvitationToken,
    val club: Club,
    val requestingContributor: SimpleContributor,
)
