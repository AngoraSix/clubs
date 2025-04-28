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
    val privateData: Map<String, Any> = mutableMapOf(),
    var status: MemberStatusValue = MemberStatusValue.INACTIVE,
) {
    override fun equals(other: Any?): Boolean = other is Member && other.contributorId == contributorId

    override fun hashCode(): Int = contributorId.hashCode()
}

enum class MemberStatusValue(
    val value: String,
) {
    ACTIVE("active"),
    INACTIVE("inactive"),
    PENDING("pending"),
    ;

    fun isActive(): Boolean = this == ACTIVE
}

enum class MemberRolesValue(
    val value: String,
) {
    ADMIN("admin"),
    CONTRIBUTOR("contributor"),
}
