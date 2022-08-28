package com.angorasix.clubs.infrastructure.presentation.rest.patch

import com.angorasix.clubs.domain.club.modification.DomainObjectModification
import com.angorasix.commons.domain.RequestingContributor
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

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

data class PatchOperation(val op: String, val path: String, val value: JsonNode?) {
    fun toDomainObjectModification(contributor: RequestingContributor, supportedOperations: List<PatchOperationSpec>, objectMapper: ObjectMapper): DomainObjectModification<out Any, out Any> {
        return supportedOperations.find { it.supportsPatchOperation(this) }?.let { it.mapToObjectModification(contributor, this, objectMapper) } ?: throw IllegalArgumentException("Patch Operation not supported")
    }
}

//data class PatchOperationSpec<T, U>(val checkFn: Predicate<PatchOperation>, val mapOperationFn: BiFunction<PatchOperation, ObjectMapper, DomainObjectModification<T, U>>)

abstract class PatchOperationSpec {
    abstract fun supportsPatchOperation(operation: PatchOperation): Boolean
    abstract fun mapToObjectModification(contributor: RequestingContributor, operation: PatchOperation, objectMapper: ObjectMapper): DomainObjectModification<out Any, out Any>
}
