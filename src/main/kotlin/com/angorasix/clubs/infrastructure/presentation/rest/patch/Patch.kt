package com.angorasix.clubs.infrastructure.presentation.rest.patch

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class Patch @JsonCreator constructor(operations: List<PatchOperation>) {

    @get:JsonValue
    val operations: List<PatchOperation> = operations

}

data class PatchOperation(val op: String, val path: String, val value: JsonNode?)
