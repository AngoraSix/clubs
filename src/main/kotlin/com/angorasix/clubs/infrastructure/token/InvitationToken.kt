package com.angorasix.clubs.infrastructure.token

import java.time.Instant

class InvitationToken(
    val email: String,
    var clubId: String,
    var tokenValue: String,
    var expirationInstant: Instant,
    var contributorId: String? = null,
)
