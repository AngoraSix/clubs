package com.angorasix.commons.presentation.dto

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class ContributorHeaderDto(
        var contributorId: String,
        var attributes: Map<String, String> = mutableMapOf(),
        var projectAdmin: Boolean = false
)