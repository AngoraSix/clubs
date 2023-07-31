package com.angorasix.clubs.infrastructure.config.clubs.wellknown

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "wellknown.roles")
data class WellKnownRoles(
    val admin: String,
    val creator: String,
)
