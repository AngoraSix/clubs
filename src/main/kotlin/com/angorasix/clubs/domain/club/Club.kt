package com.angorasix.clubs.domain.club

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
data class Club @PersistenceCreator private constructor(
        @field:Id val id: String?,
        val name: String,
        val type: String,
        val description: String,
        val projectId: String?,
        val members: MutableSet<Member> = mutableSetOf(),
        val open: Boolean, // anyone can access without invitation
        val public: Boolean, // visible for the rest
        val social: Boolean, // members can interact / see themselves
        val requirements: Set<String> = mutableSetOf(),
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
            requirements: Set<String> = mutableSetOf(),
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
            requirements,
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
}