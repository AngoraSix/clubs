package com.angorasix.clubs.presentation.dto

import java.time.ZonedDateTime

/**
 *
 *
 * @author rozagerardo
 */
data class MemberDto(
        var contributorId: String? = null,
        var roles: Collection<String> = mutableSetOf(),
)

data class ClubDto(
        val id: String? = null,
        val name: String? = null,
        val type: String,
        val description: String? = null,
        val projectId: String? = null,
        val members: MutableSet<MemberDto> = mutableSetOf(),
        val open: Boolean? = null,
        val public: Boolean? = null,
        val social: Boolean? = null,
        val requirements: Set<String> = mutableSetOf(),
        val createdAt: ZonedDateTime? = null,
)

data class ContributorHeaderDto(
        var contributorId: String,
        var attributes: Map<String, String> = mutableMapOf(),
        var projectAdmin: Boolean = false
)
