package com.angorasix.clubs.infrastructure.token

import java.time.Instant

class InvitationToken(
    val email: String,
    var clubId: String,
    var expirationInstant: Instant? = null,
    var tokenValue: String? = null,
)

data class InvitationTokenInputRequirement(var email: String)
