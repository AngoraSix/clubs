package com.angorasix.clubs.domain.club

import com.angorasix.commons.domain.SimpleContributor
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Club Aggregate Root.
 *
 * A Club will group several contributors (members) for a particular objective.
 *
 * @author rozagerardo
 */
data class Club @PersistenceCreator constructor(
    @field:Id val id: String?,
    var name: String,
    val type: String,
    val projectId: String?,
    var members: MutableSet<Member> = mutableSetOf(),
    val admins: MutableSet<SimpleContributor> = mutableSetOf(),
    val open: Boolean, // anyone can access without invitation
    val public: Boolean, // visible for the rest
    val social: Boolean, // members can interact / see themselves
    var description: String?,
    val createdAt: ZonedDateTime,
) {

    /**
     * The final constructor that sets all initial fields.
     *
     */
    constructor(
        name: String,
        type: String,
        projectId: String?,
        members: MutableSet<Member> = mutableSetOf(),
        admins: MutableSet<SimpleContributor> = mutableSetOf(),
        open: Boolean,
        public: Boolean,
        social: Boolean,
        description: String? = null,
        zone: ZoneId? = ZoneId.systemDefault(),
    ) : this(
        null,
        name,
        type,
        projectId,
        members,
        admins,
        open,
        public,
        social,
        description,
        ZonedDateTime.now(zone),
    )

    /**
     * Register new Club, setting it up appropiately.
     *
     * @param requestingContributor - contributor to be added to the 'admins' set
     */
    fun register(requestingContributor: SimpleContributor, isCreatorMember: Boolean? = false) {
        admins.add(requestingContributor)
        if (isCreatorMember == true) {
            members.add(Member(
                contributorId = requestingContributor.contributorId,
                status = MemberStatusValue.ACTIVE,
                roles = setOf(MemberRolesValue.ADMIN.value),
                data = mapOf("creator" to true),
            ))
        }
    }

    /**
     * Add a single member to the set.
     *
     * @param member - contributor to be added to the set
     */
    fun addMember(member: Member) {
        val memberToAdd = members.find { it.contributorId == member.contributorId } ?: member
        members.add(if (open) memberToAdd.copy(status = MemberStatusValue.ACTIVE) else memberToAdd)
    }

    /**
     * Remove a single member from the set.
     *
     * @param member - contributor to be removed from the set
     */
    fun removeMember(member: Member) {
        val existingMember = members.find { it.contributorId == member.contributorId }
        existingMember?.let { members.add(existingMember.copy(status = MemberStatusValue.INACTIVE)) }
    }

    /**
     * Checks whether a particular member has visibility over this project,
     * whether because it's 'public' or 'social' and the member belongs to the club.
     *
     * @param requestingContributor - contributor trying to see the Club.
     */
    fun isVisibleToContributor(requestingContributor: SimpleContributor?): Boolean = public
        .or(social.and(members.any { it.contributorId == requestingContributor?.contributorId &&  it.status.isActive() }))
        .or(isAdmin(requestingContributor?.contributorId))

    fun resolveAdmins(requestingContributor: SimpleContributor?): Set<SimpleContributor> =
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
        open.and(!members.contains(contributor))
            .and(!isAdmin(contributor.contributorId))

    /**
     * Checks whether a particular contributor can be removed as a member of this Club.
     *
     * @param contributor - contributor candidate to leave the Club.
     */
    fun canRemoveMember(contributor: Member): Boolean =
        open.and(members.contains(contributor))
            .and(!isAdmin(contributor.contributorId))

    /**
     * Checks whether a particular contributor is Admin of this Club.
     *
     * @param contributorId - contributor candidate to check.
     */
    fun isAdmin(contributorId: String?): Boolean =
        (contributorId != null).and(admins.any { it.contributorId == contributorId })
}
