package com.angorasix.clubs.infrastructure.config.token

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "configs.tokens")
class TokenConfigurations(
    val secret: String,
    val expirationTime: Long,
    val issuer: String,
)
