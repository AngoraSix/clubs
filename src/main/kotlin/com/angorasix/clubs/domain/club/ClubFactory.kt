package com.angorasix.clubs.domain.club

import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubDescription

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
object ClubFactory {
    fun fromDescription(
        clubDescription: WellKnownClubDescription,
        projectId: String?,
        projectManagementId: String?,
    ): Club =
        Club(
            name = "${projectId ?: projectManagementId}+${clubDescription.type}",
            type = clubDescription.type,
            projectId = projectId,
            projectManagementId = projectManagementId,
            members = mutableSetOf(),
            admins = mutableSetOf(),
            open = clubDescription.open,
            public = clubDescription.public,
            social = clubDescription.social,
        )
}
