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
)