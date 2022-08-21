package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.ClubFactory
import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Flux

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
    suspend fun modifyWellKnownClub(updatingContributor: Member, type: String, projectId: String?, modificationOperations: List<ClubModification<out Any>>): Club? {
        val club = (repository.findByTypeAndProjectId(type, projectId)
                ?: wellKnownClubConfigurations.clubs.wellKnownClubDescriptions[type]
                        ?.let { ClubFactory.fromDescription(it, projectId) })
        val updatedClub = club?.let { modificationOperations.fold(it) { accumulatedClub, op -> op.modify(updatingContributor, accumulatedClub) } }
        return updatedClub?.let { repository.save(updatedClub) }
    }

    /**
     * Method to add a member to a [Club]. If the club is a well-known club, then it will be created before adding the member.
     *
     */
    suspend fun updateWellKnownClub(member: Member, type: String, projectId: String?, updatedClub: Club): Club? {
        var club = repository.findByTypeAndProjectId(type, projectId)
                ?: wellKnownClubConfigurations.clubs.wellKnownClubDescriptions[type]
                        ?.let { ClubFactory.fromDescription(it, projectId) }
        return club?.update(member, updatedClub)?.let {
            repository.save(it)
        }
    }

    /**
     * Method to get a single Well-Known [Club] from a type and projectId.
     *
     */
    suspend fun findWellKnownClubForContributor(contributor: Member?, type: String, projectId: String): Club? =
            getWellKnownClub(type, projectId)//.takeIf { it?.isVisibleToMember(contributor) == true }

    /**
     * Method to get a single Well-Known [Club] from a type and projectId, without making further validations.
     *
     */
    private suspend fun getWellKnownClub(type: String, projectId: String): Club? =
            if (wellKnownClubConfigurations.clubs.wellKnownClubTypes.containsValue(type))
                repository.findByTypeAndProjectId(type, projectId)
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
    // @TODO: depurate members to avoid adding member with non-allowed roles and clean up data to allow only well-known data
        updatedMembers
    else
        originalMembers
}

private fun isModifyingUpdatingMember(updatingMember: Member, originalMembers: MutableSet<Member>, updatedMembers: MutableSet<Member>): Boolean {
    val diff1 = originalMembers.minus(updatedMembers)
    val diff2 = updatedMembers.minus(originalMembers)
    return ((diff1.size == 1) xor (diff2.size == 1) && (diff1.contains(updatingMember) || diff2.contains(updatingMember)))
}
