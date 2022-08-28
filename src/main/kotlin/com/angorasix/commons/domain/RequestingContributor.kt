package com.angorasix.commons.domain

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class RequestingContributor constructor(
        val id: String,
        val isProjectAdmin: Boolean = false
)