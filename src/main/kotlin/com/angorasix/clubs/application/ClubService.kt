package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.ClubFactory
import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service providing functionality for Clubs.
 *
 * @author rozagerardo
 */
class ClubService(private val repository: ClubRepository, val wellKnownClubConfigurations: WellKnownClubConfigurations) {

    /**
     * Method to retrieve a collection of [Club]s.
     *
     * @return [Flux] of [Club]
     */
    fun findClubs(filter: ListClubsFilter): Flow<Club> = repository.findUsingFilter(filter)

    /**
     * Method to add a member to a [Club]. If the member is a well-known club, then it will be created before adding the member.
     *
     */
    suspend fun addMemberToWellKnownClub(member: Member, type: String, projectId: String?): Club? {
        var club = repository.findByTypeAndProjectId(type, projectId)
                ?: wellKnownClubConfigurations.wellKnownClubDescriptions.find { config -> config.type == type }
                        ?.let { ClubFactory.fromDescription(it, projectId) }
        return club?.let {
            it.addMember(member)
            repository.save(it)
        }
    }

    /**
     * Method to find a single [Club] from an id.
     *
     * @param clubId [Club] id
     * @return a [Mono] with the persisted [Club]
     */
    suspend fun findWellKnownClub(type: String, projectId: String): Club? =
            if (wellKnownClubConfigurations.wellKnownClubTypes.containsValue(type))
                repository.findByTypeAndProjectId(type, projectId)
            else
                null
}
