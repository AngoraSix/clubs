package com.angorasix.clubs.domain.club

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class Member(
        var contributorId: String,
        var roles: Collection<String> = mutableSetOf(),
        var isProjectAdmin: Boolean = false
) {
    override fun equals(other: Any?): Boolean =
            other is Member && other.contributorId == contributorId

    override fun hashCode(): Int {
        return contributorId.hashCode()
    }
}