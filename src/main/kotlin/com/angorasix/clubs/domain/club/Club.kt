package com.angorasix.clubs.domain.club

import com.angorasix.commons.domain.RequestingContributor
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
    var description: String,
    val projectId: String?,
    var members: MutableSet<Member> = mutableSetOf(),
    val open: Boolean, // anyone can access without invitation
    val public: Boolean, // visible for the rest
    val social: Boolean, // members can interact / see themselves
    val createdAt: ZonedDateTime,
) {

    /**
     * The final constructor that sets all initial fields.
     *
     */
    constructor(
        name: String,
        type: String,
        description: String,
        projectId: String?,
        members: MutableSet<Member> = mutableSetOf(),
        open: Boolean,
        public: Boolean,
        social: Boolean,
        zone: ZoneId? = ZoneId.systemDefault(),
    ) : this(
        null,
        name,
        type,
        description,
        projectId,
        members,
        open,
        public,
        social,
        ZonedDateTime.now(zone)
    )

    /**
     * Add a single member to the set.
     *
     * @param member - contributor to be added to the set
     */
    fun addMember(member: Member) {
        members.add(member)
    }

    /**
     * Remove a single member from the set.
     *
     * @param member - contributor to be removed from the set
     */
    fun removeMember(member: Member) {
        members.remove(member)
    }

    /**
     * Checks whether a particular member has visibility over this project,
     * whether because it's 'public' or 'social' and the member belongs to the club.
     *
     * @param requestingContributor - contributor trying to see the Club.
     */
    fun isVisibleToContributor(requestingContributor: RequestingContributor?): Boolean = public
        .or(social.and(members.any { it.contributorId == requestingContributor?.id }))
        .or(requestingContributor?.isProjectAdmin ?: false)

    /**
     * Checks whether a particular contributor can be added as a member of this Club.
     *
     * @param contributor - contributor candidate to join the Club.
     */
    fun canAddMember(contributor: Member): Boolean =
        open.and(!members.contains(contributor)).and(!contributor.isProjectAdmin)

    /**
     * Checks whether a particular contributor can be removed as a member of this Club.
     *
     * @param contributor - contributor candidate to leave the Club.
     */
    fun canRemoveMember(contributor: Member): Boolean =
        open.and(members.contains(contributor)).and(!contributor.isProjectAdmin)

}
