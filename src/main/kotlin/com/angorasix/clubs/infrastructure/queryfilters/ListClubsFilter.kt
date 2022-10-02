package com.angorasix.clubs.infrastructure.queryfilters

/**
 * <p> Classes containing different Request Query Filters.
 * </p>
 *
 * @author rozagerardo
 */
data class ListClubsFilter(
    val projectId: Collection<String>? = null,
    val type: String? = null,
    val contributorId: String? = null,
)
