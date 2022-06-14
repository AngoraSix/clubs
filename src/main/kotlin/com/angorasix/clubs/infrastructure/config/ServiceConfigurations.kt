package com.angorasix.clubs.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@Configuration
@ConfigurationProperties(prefix = "configs")
class ServiceConfigs {
    lateinit var api: ApiConfigs
}


class ApiConfigs @ConstructorBinding constructor(var contributorHeader: String) {
}
