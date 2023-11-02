package com.angorasix.clubs.infrastructure.queryfilters

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

/**
 * <p> Classes containing different Request Query Filters.
 * </p>
 *
 * @author rozagerardo
 */
data class ListClubsFilter(
    val projectId: Collection<String>? = null,
    val type: String? = null,
    val memberContributorId: Collection<String>? = null,
    val adminId: Collection<String>? = null,
) {
    fun toMultiValueMap(): MultiValueMap<String, String> {
        val multiMap: MultiValueMap<String, String> = LinkedMultiValueMap()
        multiMap.add("projectId", projectId?.joinToString(","))
        multiMap.add("type", type)
        multiMap.add("adminId", memberContributorId?.joinToString(","))
        multiMap.add("adminId", adminId?.joinToString(","))
        return multiMap
    }

    companion object {
        fun fromMultiValueMap(multiMap: MultiValueMap<String, String>): ListClubsFilter {
            return ListClubsFilter(
                multiMap.getFirst("projectId")?.split(","),
                multiMap.getFirst("type"),
                multiMap.getFirst("memberContributorId")?.split(","),
                multiMap.getFirst("adminId")?.split(","),
            )
        }
    }
}
