package com.angorasix.clubs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.support.WebStack

@SpringBootApplication
@EnableHypermediaSupport(
    type = [EnableHypermediaSupport.HypermediaType.HAL_FORMS],
    stacks = [WebStack.WEBFLUX],
)
@ConfigurationPropertiesScan("com.angorasix.clubs.infrastructure.config")
class ClubsApplication

fun main(args: Array<String>) {
    runApplication<ClubsApplication>(args = args)
}
