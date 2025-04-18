package com.angorasix.clubs.infrastructure.persistence.repository

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.commons.domain.SimpleContributor
import kotlinx.coroutines.flow.Flow

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ClubFilterRepository {

    fun findUsingFilter(
        filter: ListClubsFilter,
        requestingContributor: SimpleContributor?,
    ): Flow<Club>

    suspend fun addMemberToClub(
        clubId: String,
        member: Member,
        requestingContributor: SimpleContributor,
        fromInvitation: Boolean = false,
    ): Club?
}
