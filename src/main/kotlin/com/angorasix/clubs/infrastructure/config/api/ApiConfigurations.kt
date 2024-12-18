package com.angorasix.clubs.infrastructure.config.api

import com.angorasix.commons.infrastructure.config.configurationproperty.api.Route
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "configs.api")
class ApiConfigs(

    @NestedConfigurationProperty
    var routes: RoutesConfigs,

    @NestedConfigurationProperty
    var basePaths: BasePathConfigs,

    @NestedConfigurationProperty
    var clubActions: ClubActions,
)

class BasePathConfigs(val clubs: String, val wellKnown: String)

class RoutesConfigs(
    val wellKnownGetSingle: Route,
    val wellKnownGetForProject: Route,
    val wellKnownSearch: Route,
    val wellKnownPatch: Route,
    val wellKnownRegister: Route,
)

class ClubActions(
    val addMember: String,
    val removeMember: String,
    val registerAll: String,
)
