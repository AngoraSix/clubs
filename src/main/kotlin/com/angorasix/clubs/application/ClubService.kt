package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.ClubFactory
import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubDescription
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.commons.domain.SimpleContributor
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Flux

/**
 * Service providing functionality for Clubs.
 *
 * @author rozagerardo
 */
class ClubService(
    private val repository: ClubRepository,
    private val wellKnownClubConfigurations: WellKnownClubConfigurations,
) {

    /**
     * Method to register all well-known [Club]s for a Project.
     * New clubs will be created, with the requesting contributor as Admin.
     *
     */
    suspend fun registerAllWellKnownClub(
        requestingContributor: SimpleContributor,
        projectId: String,
    ): List<Club> {
        return wellKnownClubConfigurations.wellKnownClubDescriptions.values.map { description ->
            repository.findByTypeAndProjectId(description.type, projectId)
                ?: registerNewWellKnownClub(description, projectId, requestingContributor)
        }
    }

    private suspend fun registerNewWellKnownClub(
        description: WellKnownClubDescription,
        projectId: String,
        requestingContributor: SimpleContributor,
    ): Club {
        val newWellKnownClub = ClubFactory.fromDescription(
            description,
            projectId,
        )
        newWellKnownClub.register(requestingContributor)
        return repository.save(newWellKnownClub)
    }

//    private suspend fun registerWellKnownClub(
//        requestingContributor: SimpleContributor,
//        projectId: String?,
//    ): Flow<Club>? {
//        return wellKnownClubConfigurations.clubs.wellKnownClubDescriptions.values.map { description ->
//            repository.findByTypeAndProjectId(description.type, projectId) ?: {
//                val newWellKnownClub = ClubFactory.fromDescription(
//                    description,
//                    projectId,
//                )
//                newWellKnownClub.register(requestingContributor)
//                repository.save(newWellKnownClub)
//            }
//        }.asFlow()
//    }

    /**
     * Method to retrieve a collection of [Club]s.
     *
     * @return [Flux] of [Club]
     */
    fun findClubs(
        filter: ListClubsFilter,
        requestingContributor: SimpleContributor?,
    ): Flow<Club> = repository.findUsingFilter(filter, requestingContributor)

    /**
     * Method to add a member to a [Club].
     * If the club is a well-known club, then it will be created before adding the member.
     *
     */
    suspend fun modifyWellKnownClub(
        requestingContributor: SimpleContributor,
        type: String,
        projectId: String?,
        modificationOperations: List<ClubModification<out Any>>,
    ): Club? {
        val club = repository.findByTypeAndProjectId(type, projectId)
            ?: wellKnownClubConfigurations.wellKnownClubDescriptions[type]?.let {
                ClubFactory.fromDescription(
                    it,
                    projectId,
                )
            }
        val updatedClub = club?.let {
            modificationOperations.fold(it) { accumulatedClub, op ->
                op.modify(
                    requestingContributor,
                    accumulatedClub,
                )
            }
        }
        return updatedClub?.let { repository.save(updatedClub) }
    }

    /**
     * Method to add a member to a [Club].
     * If the club is a well-known club, then it will be created before adding the member.
     *
     */
    suspend fun updateWellKnownClub(
        member: Member,
        type: String,
        projectId: String?,
        updatedClub: Club,
    ): Club? {
        var club = repository.findByTypeAndProjectId(type, projectId)
            ?: wellKnownClubConfigurations.wellKnownClubDescriptions[type]?.let {
                ClubFactory.fromDescription(
                    it,
                    projectId,
                )
            }
        return club?.update(member, updatedClub)?.let {
            repository.save(it)
        }
    }

    /**
     * Method to get a single Well-Known [Club] from a type and projectId, without making further validations.
     *
     */
    suspend fun getWellKnownClub(type: String, projectId: String): Club? =
        if (wellKnownClubConfigurations.wellKnownClubTypes.containsValue(type)) {
            repository.findByTypeAndProjectId(
                type,
                projectId,
            ) ?: wellKnownClubConfigurations.wellKnownClubDescriptions[type]?.let {
                ClubFactory.fromDescription(
                    it,
                    projectId,
                )
            }
        } else {
            null
        }
}

private fun Club.update(
    updatingMember: Member,
    updatedData: Club,
    wellKnown: Boolean = true,
): Club {
    val isProjectAdmin = isAdmin(updatingMember.contributorId)
    if (!wellKnown && isProjectAdmin) {
        name = updatedData.name
        description = updatedData.description
    }
    members = checkedUpdatedMembers(updatingMember, members, updatedData.members, isProjectAdmin)
    return this
}

private fun checkedUpdatedMembers(
    updatingMember: Member,
    originalMembers: MutableSet<Member>,
    updatedMembers: MutableSet<Member>,
    isProjectAdmin: Boolean,
): MutableSet<Member> {
    if (isProjectAdmin) return updatedMembers
    return if (isModifyingUpdatingMember(updatingMember, originalMembers, updatedMembers)) {
        // @to-do:
        // depurate members to avoid adding member with non-allowed roles
        // and clean up data to allow only well-known data
        updatedMembers
    } else {
        originalMembers
    }
}

private fun isModifyingUpdatingMember(
    updatingMember: Member,
    originalMembers: MutableSet<Member>,
    updatedMembers: MutableSet<Member>,
): Boolean {
    val diff1 = originalMembers.minus(updatedMembers)
    val diff2 = updatedMembers.minus(originalMembers)

    val bothHaveJustOneUpdatedMember =
        { set1: Set<Member>, set2: Set<Member> -> (set1.size == 1) xor (set2.size == 1) }
    val anyContainsUpdatingMember =
        { set1: Set<Member>, set2: Set<Member>, member: Member ->
            set1.contains(updatingMember) || set2.contains(
                member,
            )
        }
    return bothHaveJustOneUpdatedMember(diff1, diff2) && anyContainsUpdatingMember(
        diff1,
        diff2,
        updatingMember,
    )
}
