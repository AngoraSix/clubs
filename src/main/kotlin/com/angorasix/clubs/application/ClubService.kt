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
class ClubService(private val repository: ClubRepository, private val wellKnownClubConfigurations: WellKnownClubConfigurations) {

    /**
     * Method to retrieve a collection of [Club]s.
     *
     * @return [Flux] of [Club]
     */
    fun findClubs(filter: ListClubsFilter): Flow<Club> = repository.findUsingFilter(filter)

    /**
     * Method to add a member to a [Club]. If the club is a well-known club, then it will be created before adding the member.
     *
     */
    suspend fun addMemberToWellKnownClub(member: Member, type: String, projectId: String?): Club? {
        if (member.isProjectAdmin) throw IllegalArgumentException("Can't add Admin as member")
        var club = repository.findByTypeAndProjectId(type, projectId)
                ?: wellKnownClubConfigurations.clubs.wellKnownClubDescriptions[type]
                        ?.let { ClubFactory.fromDescription(it, projectId) }
        return club?.let {
            it.addMember(member)
            repository.save(it)
        }
    }

    /**
     * Method to remove a member from a [Club].
     *
     */
    suspend fun removeMemberFromWellKnownClub(member: Member, type: String, projectId: String?): Club? {
        if (member.isProjectAdmin) throw IllegalArgumentException("Can't Remove Admin as member")
        var club = repository.findByTypeAndProjectId(type, projectId)
        return club?.let {
            it.removeMember(member)
            repository.save(it)
        }
    }

    /**
     * Method to find a single [Club] from an id.
     *
     * @param clubId [Club] id
     * @return a [Mono] with the persisted [Club]
     */
    suspend fun findWellKnownClub(type: String, projectId: String, contributor: Member?): Club? =
            if (wellKnownClubConfigurations.clubs.wellKnownClubTypes.containsValue(type))
                repository.findByTypeAndProjectId(type, projectId).takeIf { it?.isVisibleToMember(contributor) ?: false }
            else
                null
}
