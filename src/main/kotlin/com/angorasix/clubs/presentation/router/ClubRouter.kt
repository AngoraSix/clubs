package com.angorasix.clubs.presentation.router

import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.presentation.handler.InvitationsHandler
import com.angorasix.clubs.presentation.handler.WellKnownClubHandler
import com.angorasix.commons.reactive.presentation.filter.extractRequestingContributor
import org.springframework.web.reactive.function.server.coRouter

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ClubRouter(
    private val wellKnownClubsHandler: WellKnownClubHandler,
    private val invitationHandler: InvitationsHandler,
    private val apiConfigs: ApiConfigs,
) {
    /**
     * Main RouterFunction configuration for all endpoints related to Clubs.
     *
     * @return the RouterFunction with all the routes for Clubs
     */
    fun clubRouterFunction() =
        coRouter {
            apiConfigs.basePaths.clubs.nest {
                filter { request, next ->
                    extractRequestingContributor(
                        request,
                        next,
                    )
                }
                apiConfigs.basePaths.baseWellKnown.nest {
                    apiConfigs.basePaths.baseByProjectId.nest {
                        path(apiConfigs.basePaths.baseByClubType).nest {
                            method(apiConfigs.routes.wellKnownGetForProjectAndType.method, wellKnownClubsHandler::getWellKnownClub)
                            method(apiConfigs.routes.wellKnownPatchForProjectAndType.method, wellKnownClubsHandler::patchWellKnownClub)
                        }
                        method(apiConfigs.routes.wellKnownGetForProject.method, wellKnownClubsHandler::getWellKnownClubs)
                        method(apiConfigs.routes.wellKnownRegisterForProject.method, wellKnownClubsHandler::registerWellKnownClubs)
                    }
                    apiConfigs.basePaths.baseByManagementId.nest {
                        path(apiConfigs.basePaths.baseByClubType).nest {
                            method(apiConfigs.routes.wellKnownGetForManagementAndType.method, wellKnownClubsHandler::getWellKnownClub)
                            method(apiConfigs.routes.wellKnownPatchForManagementAndType.method, wellKnownClubsHandler::patchWellKnownClub)
                        }
                        method(apiConfigs.routes.wellKnownRegisterForManagement.method, wellKnownClubsHandler::registerWellKnownClubs)
                        method(apiConfigs.routes.wellKnownGetForProjectManagement.method, wellKnownClubsHandler::getWellKnownClubs)
                    }
                    method(apiConfigs.routes.wellKnownSearch.method, wellKnownClubsHandler::searchWellKnownClubs)
                }
                apiConfigs.basePaths.baseByIdRoute.nest {
                    apiConfigs.basePaths.baseInvitations.nest {
                        path(apiConfigs.routes.addMemberFromInvitationToken.path).nest {
                            method(apiConfigs.routes.addMemberFromInvitationToken.method, invitationHandler::addMemberFromInvitationToken)
                        }
                        method(apiConfigs.routes.inviteContributor.method, invitationHandler::inviteContributor)
                    }
                }
            }
        }
}
