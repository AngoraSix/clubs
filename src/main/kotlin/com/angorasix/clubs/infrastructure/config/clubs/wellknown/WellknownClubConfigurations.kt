package com.angorasix.clubs.infrastructure.config.clubs.wellknown

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@Configuration
@ConfigurationProperties(prefix = "other")
class WellknownClubConfigurations {
    lateinit var wellKnownClubTypes: Map<String, String>
    lateinit var wellKnownClubDescriptions: Collection<WellKnownClubDescription>
}


class WellKnownClubDescription @ConstructorBinding constructor(var type: String,
                                                               var description: String,
                                                               var open: Boolean,
                                                               var public: Boolean,
                                                               var social: Boolean,
                                                               var requirements: Set<String>)