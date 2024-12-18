package com.angorasix.clubs.domain.club

import java.time.Instant

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
    val status: MemberStatusValue = MemberStatusValue.INACTIVE,
) {

    override fun equals(other: Any?): Boolean =
        other is Member && other.contributorId == contributorId

    override fun hashCode(): Int {
        return contributorId.hashCode()
    }
}

//data class MemberStatus(
//    val status: MemberStatusValue = MemberStatusValue.INACTIVE,
//    val invitationToken : MemberInvitationToken? = null
//)

enum class MemberStatusValue(value: String) {
    ACTIVE("active"),
    INACTIVE("inactive"),
    PENDING("pending");

    fun isActive(): Boolean = this == ACTIVE
}
//data class MemberInvitationToken(
//    val value: String,
//    val expirationInstant : Instant
//    val
//)
