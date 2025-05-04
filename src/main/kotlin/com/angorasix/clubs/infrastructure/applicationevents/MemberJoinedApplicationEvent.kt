package com.angorasix.clubs.infrastructure.applicationevents

import com.angorasix.clubs.domain.club.Club
import com.angorasix.commons.domain.A6Contributor

class MemberJoinedApplicationEvent(
    val memberContributorId: String,
    val club: Club,
    val requestingContributor: A6Contributor,
)
