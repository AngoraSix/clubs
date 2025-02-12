package com.angorasix.clubs.infrastructure.config.security

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * <p>
 *  Base file containing all Service Security configurations.
 * </p>
 *
 * @author rozagerardo
 */

@ConfigurationProperties(prefix = "configs.security")
class SecurityConfigurations(
    var secretKey: String,
)
