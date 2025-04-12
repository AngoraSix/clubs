package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.ClubFactory
import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.MemberStatusValue
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubDescription
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.infrastructure.security.TokenEncryptionUtil
import com.angorasix.commons.domain.SimpleContributor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.singleOrNull
import reactor.core.publisher.Flux

/**
 * Service providing functionality for Clubs.
 *
 * @author rozagerardo
 */
class ClubService(
    private val repository: ClubRepository,
    private val invitationTokenService: InvitationTokenService,
    private val encryptionUtils: TokenEncryptionUtil,
    private val wellKnownClubConfigurations: WellKnownClubConfigurations,
) {
    /**
     * Method to register all well-known [Club]s for a Project.
     * New clubs will be created, with the requesting contributor as Admin.
     *
     */
    suspend fun registerAllWellKnownClub(
        requestingContributor: SimpleContributor,
        projectId: String?,
        projectManagementId: String?,
    ): List<Club> =
        wellKnownClubConfigurations.wellKnownClubDescriptions.values.mapNotNull { description ->
            repository.findByTypeAndProjectId(description.type, projectId)
                ?: registerNewWellKnownClub(description, projectId, projectManagementId, requestingContributor)
        }

    private suspend fun registerNewWellKnownClub(
        description: WellKnownClubDescription,
        projectId: String?,
        projectManagementId: String?,
        requestingContributor: SimpleContributor,
    ): Club? {
        val newWellKnownClub =
            ClubFactory.fromDescription(
                description,
                projectId,
                projectManagementId,
            )
        return if (description.isProjectClub && projectId != null || description.isProjectManagementClub && projectManagementId != null) {
            newWellKnownClub.register(requestingContributor, description.isCreatorMember)
            repository.save(newWellKnownClub)
        } else {
            null
        }
    }

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
     * Method to modify a [Club] with [ClubModification]s.
     * If the club is a well-known club, then it will be created before modifying.
     *
     */
    suspend fun modifyWellKnownClub(
        requestingContributor: SimpleContributor,
        type: String,
        projectId: String?,
        projectManagementId: String?,
        modificationOperations: List<ClubModification<out Any>>,
    ): Club? {
        val club =
            repository.findByTypeAndProjectId(type, projectId)
                ?: wellKnownClubConfigurations.wellKnownClubDescriptions[type]?.let {
                    ClubFactory.fromDescription(
                        it,
                        projectId,
                        projectManagementId,
                    )
                }
        val updatedClub =
            club?.let {
                modificationOperations.fold(it) { accumulatedClub, op ->
                    op.modify(
                        requestingContributor,
                        accumulatedClub,
                    )
                }
            }
        return updatedClub?.let { repository.save(updatedClub) }
    }

    suspend fun addMemberFromInvitationToken(
        tokenValue: String,
        clubId: String,
        requestingContributor: SimpleContributor,
    ): Club? =
        invitationTokenService
            .checkInvitationToken(
                tokenValue,
            )?.takeUnless {
                it.clubId != clubId ||
                    (it.contributorId != null && requestingContributor.contributorId != it.contributorId)
            }?.let {
                val member =
                    Member(
                        contributorId = requestingContributor.contributorId,
                        roles = emptyList(),
                        data = mapOf("invited" to true),
                        privateData = mapOf("invitedEmail" to encryptionUtils.encrypt(it.email)),
                        status = MemberStatusValue.ACTIVE,
                    )
                repository.addMemberToClub(
                    clubId = it.clubId,
                    member = member,
                    requestingContributor = requestingContributor,
                    fromInvitation = true,
                )
            }

    /**
     * Method to get a single Well-Known [Club] from a type and projectId, without making further validations.
     *
     */
    suspend fun getWellKnownClub(
        type: String,
        projectId: String,
        projectManagementId: String,
        requestingContributor: SimpleContributor?,
    ): Club? {
        val filter =
            ListClubsFilter(
                type = type,
                projectId = listOf(projectId),
                projectManagementId = listOf(projectManagementId),
            )
        return repository.findUsingFilter(filter, requestingContributor).singleOrNull()
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
        members =
            checkedUpdatedMembers(updatingMember, members, updatedData.members, isProjectAdmin)
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
                set1.contains(updatingMember) ||
                    set2.contains(
                        member,
                    )
            }
        return bothHaveJustOneUpdatedMember(diff1, diff2) &&
            anyContainsUpdatingMember(
                diff1,
                diff2,
                updatingMember,
            )
    }
}
