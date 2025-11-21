package com.angorasix.clubs.application

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.ClubFactory
import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.MemberStatusValue
import com.angorasix.clubs.domain.club.modification.AddMember
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.infrastructure.applicationevents.MemberJoinedApplicationEvent
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubDescription
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.clubs.infrastructure.security.TokenEncryptionUtil
import com.angorasix.commons.domain.A6Contributor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.singleOrNull
import org.springframework.context.ApplicationEventPublisher
import reactor.core.publisher.Flux

/**
 * Service providing functionality for Clubs.
 *
 * @author rozagerardo
 */
class ClubService(
    private val repository: ClubRepository,
    private val invitationTokenService: InvitationTokenService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val encryptionUtils: TokenEncryptionUtil,
    private val wellKnownClubConfigurations: WellKnownClubConfigurations,
) {
    /**
     * Method to register all well-known [Club]s for a Project.
     * New clubs will be created, with the requesting contributor as Admin.
     *
     */
    suspend fun registerAllWellKnownClub(
        requestingContributor: A6Contributor,
        projectId: String? = null,
        projectManagementId: String? = null,
    ): List<Club> =
        wellKnownClubConfigurations.wellKnownClubDescriptions.values.mapNotNull { description ->
            repository.findSingleUsingFilter(
                ListClubsFilter(
                    type = description.type,
                    projectId = projectId?.let(::listOf),
                    projectManagementId = projectManagementId?.let(::listOf),
                ),
                requestingContributor,
            )
                ?: registerNewWellKnownClub(description, projectId, projectManagementId, requestingContributor)
        }

    private suspend fun registerNewWellKnownClub(
        description: WellKnownClubDescription,
        projectId: String?,
        projectManagementId: String?,
        requestingContributor: A6Contributor,
    ): Club? {
        val newWellKnownClub =
            ClubFactory.fromDescription(
                description,
                projectId,
                projectManagementId,
            )

        val isProjectClubValid = description.isProjectClub && projectId != null
        val isMgmtClubValid = description.isProjectManagementClub && projectManagementId != null

        return if (isProjectClubValid || isMgmtClubValid) {
            newWellKnownClub.register(requestingContributor, description.isCreatorMember)
            val persistedNewClub = repository.save(newWellKnownClub)

            if (description.isCreatorMember) {
                applicationEventPublisher.publishEvent(
                    MemberJoinedApplicationEvent(
                        memberContributorId = requestingContributor.contributorId,
                        club = persistedNewClub,
                        requestingContributor = requestingContributor,
                    ),
                )
            }
            persistedNewClub
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
        requestingContributor: A6Contributor?,
    ): Flow<Club> = repository.findUsingFilter(filter, requestingContributor).onEach { it.hideInactiveMembers() }

    /**
     * Method to modify a [Club] with [ClubModification]s.
     * If the club is a well-known club, then it will be created before modifying.
     *
     */
    suspend fun modifyWellKnownClub(
        requestingContributor: A6Contributor,
        type: String,
        projectId: String?,
        projectManagementId: String?,
        modificationOperations: List<ClubModification<out Any>>,
    ): Club? {
        val club =
            repository.findSingleUsingFilter(
                ListClubsFilter(
                    type = type,
                    projectId = projectId?.let(::listOf),
                    projectManagementId = projectManagementId?.let(::listOf),
                ),
                requestingContributor,
            )
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
        return updatedClub?.let {
            repository.save(it).also {
                modificationOperations.filterIsInstance<AddMember>().forEach { op ->
                    applicationEventPublisher.publishEvent(
                        MemberJoinedApplicationEvent(
                            memberContributorId = op.modifyValue.contributorId,
                            club = it,
                            requestingContributor = requestingContributor,
                        ),
                    )
                }
            }
        }
    }

    suspend fun addMemberFromInvitationToken(
        tokenValue: String,
        clubId: String,
        requestingContributor: A6Contributor,
    ): Club? {
        // 1. Validate token
        return invitationTokenService
            .checkInvitationToken(tokenValue)
            ?.takeUnless {
                it.clubId != clubId ||
                    (
                        it.contributorId != null &&
                            it.contributorId != requestingContributor.contributorId
                    )
            }?.let {
                // 2. Check if already member
                val existing =
                    repository.findClubWhereMemberExists(
                        clubId = clubId,
                        contributorId = requestingContributor.contributorId,
                    )
                if (existing != null) {
                    return existing // already a member → idempotent success
                }

                // 3. Build member
                val member =
                    Member(
                        contributorId = requestingContributor.contributorId,
                        roles = emptyList(),
                        data = mapOf("invited" to true),
                        privateData =
                            mapOf(
                                "invitedEmail" to encryptionUtils.encrypt(it.email),
                            ),
                        status = MemberStatusValue.ACTIVE,
                    )

                // 4. Try to add new member
                val updatedClub =
                    repository.addMemberToClub(
                        clubId = it.clubId,
                        member = member,
                        requestingContributor = requestingContributor,
                        fromInvitation = true,
                    )
                if (updatedClub !== null) {
                    // 5. Publish event
                    applicationEventPublisher.publishEvent(
                        MemberJoinedApplicationEvent(
                            memberContributorId = member.contributorId,
                            club = updatedClub,
                            requestingContributor = requestingContributor,
                        ),
                    )
                }
                updatedClub
            }
    }

    /**
     * Method to get a single Well-Known [Club] from a type and projectId, without making further validations.
     *
     */
    suspend fun getWellKnownClub(
        type: String,
        projectId: String?,
        projectManagementId: String?,
        requestingContributor: A6Contributor?,
    ): Club? {
        val filter =
            ListClubsFilter(
                type = type,
                projectId = projectId?.let(::listOf),
                projectManagementId = projectManagementId?.let(::listOf),
            )
        return repository.findUsingFilter(filter, requestingContributor).singleOrNull()
    }
//
//    private fun Club.update(
//        updatingMember: Member,
//        updatedData: Club,
//        wellKnown: Boolean = true,
//    ): Club {
//        val isProjectAdmin = isAdmin(updatingMember.contributorId)
//        if (!wellKnown && isProjectAdmin) {
//            name = updatedData.name
//            description = updatedData.description
//        }
//        members =
//            checkedUpdatedMembers(updatingMember, members, updatedData.members, isProjectAdmin)
//        return this
//    }
//
//    private fun checkedUpdatedMembers(
//        updatingMember: Member,
//        originalMembers: MutableSet<Member>,
//        updatedMembers: MutableSet<Member>,
//        isProjectAdmin: Boolean,
//    ): MutableSet<Member> {
//        if (isProjectAdmin) return updatedMembers
//        return if (isModifyingUpdatingMember(updatingMember, originalMembers, updatedMembers)) {
//            // @to-do:
//            // depurate members to avoid adding member with non-allowed roles
//            // and clean up data to allow only well-known data
//            updatedMembers
//        } else {
//            originalMembers
//        }
//    }
//
//    private fun isModifyingUpdatingMember(
//        updatingMember: Member,
//        originalMembers: MutableSet<Member>,
//        updatedMembers: MutableSet<Member>,
//    ): Boolean {
//        val diff1 = originalMembers.minus(updatedMembers)
//        val diff2 = updatedMembers.minus(originalMembers)
//
//        val bothHaveJustOneUpdatedMember =
//            { set1: Set<Member>, set2: Set<Member> -> (set1.size == 1) xor (set2.size == 1) }
//        val anyContainsUpdatingMember =
//            { set1: Set<Member>, set2: Set<Member>, member: Member ->
//                set1.contains(updatingMember) ||
//                    set2.contains(
//                        member,
//                    )
//            }
//        return bothHaveJustOneUpdatedMember(diff1, diff2) &&
//            anyContainsUpdatingMember(
//                diff1,
//                diff2,
//                updatingMember,
//            )
//    }
}

private fun Club.hideInactiveMembers() {
    members.removeIf { it.status == MemberStatusValue.INACTIVE || it.status == MemberStatusValue.PENDING }
}
