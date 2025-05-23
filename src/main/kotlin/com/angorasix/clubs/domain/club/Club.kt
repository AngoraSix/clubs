package com.angorasix.clubs.domain.club

import com.angorasix.commons.domain.A6Contributor
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import java.time.Instant

/**
 * Club Aggregate Root.
 *
 * A Club will group several contributors (members) for a particular objective.
 *
 * @author rozagerardo
 */
data class Club
    @PersistenceCreator
    constructor(
        @field:Id val id: String?,
        var name: String,
        val type: String,
        val projectId: String?,
        val projectManagementId: String?,
        var members: MutableSet<Member> = mutableSetOf(),
        val admins: MutableSet<A6Contributor> = mutableSetOf(),
        val open: Boolean, // anyone can access without invitation
        val public: Boolean, // visible for the rest
        val social: Boolean, // members can interact / see themselves
        var description: String?,
        val createdInstant: Instant,
    ) {
        /**
         * The final constructor that sets all initial fields.
         *
         */
        constructor(
            name: String,
            type: String,
            projectId: String?,
            projectManagementId: String?,
            members: MutableSet<Member> = mutableSetOf(),
            admins: MutableSet<A6Contributor> = mutableSetOf(),
            open: Boolean,
            public: Boolean,
            social: Boolean,
            description: String? = null,
        ) : this(
            null,
            name,
            type,
            projectId,
            projectManagementId,
            members,
            admins,
            open,
            public,
            social,
            description,
            Instant.now(),
        )

        /**
         * Register new Club, setting it up appropiately.
         *
         * @param requestingContributor - contributor to be added to the 'admins' set
         */
        fun register(
            requestingContributor: A6Contributor,
            isCreatorMember: Boolean? = false,
        ) {
            admins.add(requestingContributor)
            if (isCreatorMember == true) {
                members.add(
                    Member(
                        contributorId = requestingContributor.contributorId,
                        status = MemberStatusValue.ACTIVE,
                        roles = setOf(MemberRolesValue.ADMIN.value),
                        data = mapOf("creator" to true),
                    ),
                )
            }
        }

        /**
         * Add a single member to the set.
         *
         * @param member - contributor to be added to the set
         */
        fun addMember(member: Member) {
            val existingMember = members.find { it.contributorId == member.contributorId }
            val updatedMember =
                existingMember?.let {
                    members.remove(it)
                    it.copy(
                        roles = member.roles,
                        data = existingMember.data + member.data,
                        privateData = existingMember.privateData + member.privateData,
                    )
                } ?: member
            updatedMember.status = if (open) MemberStatusValue.ACTIVE else member.status
            members.add(updatedMember)
        }

        /**
         * Remove a single member from the set.
         *
         * @param member - contributor to be removed from the set
         */
        fun removeMember(member: Member) {
            val existingMember = members.find { it.contributorId == member.contributorId }
            existingMember?.let { it.status = MemberStatusValue.INACTIVE }
        }

        /**
         * Checks whether a particular member has visibility over this project,
         * whether because it's 'public' or 'social' and the member belongs to the club.
         *
         * @param requestingContributor - contributor trying to see the Club.
         */
        fun isVisibleToContributor(requestingContributor: A6Contributor?): Boolean =
            public
                .or(
                    social.and(
                        members.any
                            {
                                it.contributorId == requestingContributor?.contributorId &&
                                    it.status.isActive()
                            },
                    ),
                ).or(isAdmin(requestingContributor?.contributorId))

        fun resolveAdmins(requestingContributor: A6Contributor?): Set<A6Contributor> =
            if (isVisibleToContributor(requestingContributor)) {
                admins
            } else {
                admins.filter { it.contributorId == requestingContributor?.contributorId }.toSet()
            }

        /**
         * Checks whether a particular contributor can be added as a member of this Club.
         *
         * @param contributor - contributor candidate to join the Club.
         */
        fun canAddMember(contributor: Member): Boolean =
            open
                .and(
                    (!members.contains(contributor))
                        .or(
                            members.find { it.contributorId == contributor.contributorId }?.status == MemberStatusValue.INACTIVE,
                        ),
                ).and(!isAdmin(contributor.contributorId))

        /**
         * Checks whether a particular contributor can be removed as a member of this Club.
         *
         * @param contributor - contributor candidate to leave the Club.
         */
        fun canRemoveMember(contributor: Member): Boolean =
            open
                .and(
                    members
                        .contains(contributor)
                        .and(
                            members.find { it.contributorId == contributor.contributorId }?.status == MemberStatusValue.ACTIVE,
                        ),
                ).and(!isAdmin(contributor.contributorId))

        /**
         * Checks whether a particular contributor is Admin of this Club.
         *
         * @param contributorId - contributor candidate to check.
         */
        fun isAdmin(contributorId: String?): Boolean = (contributorId != null).and(admins.any { it.contributorId == contributorId })
    }
