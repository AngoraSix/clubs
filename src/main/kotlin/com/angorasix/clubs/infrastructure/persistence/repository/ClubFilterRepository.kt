package com.angorasix.clubs.infrastructure.persistence.repository

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import kotlinx.coroutines.flow.Flow

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ClubFilterRepository {

    fun findUsingFilter(filter: ListClubsFilter): Flow<Club>
}
