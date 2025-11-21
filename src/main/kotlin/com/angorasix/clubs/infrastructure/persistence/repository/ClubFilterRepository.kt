package com.angorasix.clubs.infrastructure.persistence.repository

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.commons.domain.A6Contributor
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
        requestingContributor: A6Contributor?,
    ): Flow<Club>

    suspend fun findSingleUsingFilter(
        filter: ListClubsFilter,
        requestingContributor: A6Contributor?,
    ): Club?

    suspend fun addMemberToClub(
        clubId: String,
        member: Member,
        requestingContributor: A6Contributor,
        fromInvitation: Boolean = false,
    ): Club?

    suspend fun findClubWhereMemberExists(
        clubId: String,
        contributorId: String,
    ): Club?
}
