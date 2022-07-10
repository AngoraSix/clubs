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
    suspend fun updateWellKnownClub(member: Member, type: String, projectId: String?, updatedClub: Club): Club? {
        if (member.isProjectAdmin) throw IllegalArgumentException("Can't add Admin as member")
        var club = repository.findByTypeAndProjectId(type, projectId)
                ?: wellKnownClubConfigurations.clubs.wellKnownClubDescriptions[type]
                        ?.let { ClubFactory.fromDescription(it, projectId) }
        return club?.update(member, updatedClub)?.let {
            repository.save(it)
        }
    }

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
    suspend fun findWellKnownClub(contributor: Member?, type: String, projectId: String): Club? =
            if (wellKnownClubConfigurations.clubs.wellKnownClubTypes.containsValue(type))
                repository.findByTypeAndProjectId(type, projectId).takeIf { it?.isVisibleToMember(contributor) ?: false }
            else
                null
}

private fun Club.update(updatingMember: Member, updatedData: Club, wellKnown: Boolean = true): Club {
    if (!wellKnown && updatingMember.isProjectAdmin) {
        name = updatedData.name
        description = updatedData.description
    }
    members = checkedUpdatedMembers(updatingMember, members, updatedData.members)
    return this;
}

private fun checkedUpdatedMembers(updatingMember: Member, originalMembers: MutableSet<Member>, updatedMembers: MutableSet<Member>): MutableSet<Member> {
    if (updatingMember.isProjectAdmin) return updatedMembers
    return if (isModifyingUpdatingMember(updatingMember, originalMembers, updatedMembers))
        // @TODO: depurate members to avoid adding member with non-allowed roles
        updatedMembers
    else
        originalMembers
}

private fun isModifyingUpdatingMember(updatingMember: Member, originalMembers: MutableSet<Member>, updatedMembers: MutableSet<Member>): Boolean {
    val diff1 = originalMembers.minus(updatedMembers)
    val diff2 = updatedMembers.minus(originalMembers)
    return ((diff1.size == 1) xor (diff2.size == 1) && (diff1.contains(updatingMember) || diff2.contains(updatingMember)))
}
