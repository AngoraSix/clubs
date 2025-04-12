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

class BasePathConfigs(
    val clubs: String,
    val baseWellKnown: String,
    val baseByIdRoute: String,
    val baseByProjectId: String,
    val baseByManagementId: String,
    val baseByClubType: String,
    val baseInvitations: String,
)

class RoutesConfigs(
    val wellKnownGetForProjectAndType: Route,
    val wellKnownGetForManagementAndType: Route,
    val wellKnownGetForProject: Route,
    val wellKnownGetForProjectManagement: Route,
    val wellKnownSearch: Route,
    val wellKnownPatchForProjectAndType: Route,
    val wellKnownPatchForManagementAndType: Route,
    val wellKnownRegisterForProject: Route,
    val wellKnownRegisterForManagement: Route,
    val inviteContributor: Route,
    val addMemberFromInvitationToken: Route,
)

class ClubActions(
    val addMember: String,
    val removeMember: String,
    val registerAll: String,
    val inviteContributor: String,
)
