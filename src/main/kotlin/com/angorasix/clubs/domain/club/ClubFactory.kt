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
    ): Club {
        return Club(
            "$projectId-${clubDescription.type}",
            clubDescription.type,
            clubDescription.description,
            projectId,
            mutableSetOf(),
            clubDescription.open,
            clubDescription.public,
            clubDescription.social,
        )
    }
}
