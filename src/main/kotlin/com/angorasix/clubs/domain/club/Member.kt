package com.angorasix.clubs.domain.club

import org.springframework.data.annotation.PersistenceCreator

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class Member constructor(
        val contributorId: String,
        val roles: Collection<String> = mutableSetOf(),
        val data: Map<String, Any> = mutableMapOf(),
        @field:org.springframework.data.annotation.Transient val isProjectAdmin: Boolean = false,
) {


    @PersistenceCreator
    private constructor(
            contributorId: String,
            roles: Collection<String> = mutableSetOf(),
            data: Map<String, Any> = mutableMapOf()
    ) : this(contributorId, roles, data, false)

    override fun equals(other: Any?): Boolean =
            other is Member && other.contributorId == contributorId

    override fun hashCode(): Int {
        return contributorId.hashCode()
    }
}