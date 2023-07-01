package com.angorasix.clubs.infrastructure.config.clubs.wellknown

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "wellknown.roles")
@ConstructorBinding
data class WellKnownRoles(
    val admin: String,
    val creator: String
)