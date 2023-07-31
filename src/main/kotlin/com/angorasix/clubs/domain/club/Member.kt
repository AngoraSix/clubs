package com.angorasix.clubs.domain.club

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class Member(
    val contributorId: String,
    val roles: Collection<String> = mutableSetOf(),
    val data: Map<String, Any> = mutableMapOf(),
) {

    override fun equals(other: Any?): Boolean =
        other is Member && other.contributorId == contributorId

    override fun hashCode(): Int {
        return contributorId.hashCode()
    }
}
