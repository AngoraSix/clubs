package com.angorasix.clubs.infrastructure.presentation.rest.patch

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import java.util.function.BiFunction
import java.util.function.Predicate
import java.util.stream.Collectors


/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */

abstract class DtoObjectPatch<U : Any> {

    @get:JsonValue
    private val operations: List<PatchOperation>

    @JsonCreator
    constructor(operations: List<DefaultPatchOperation>) {
        this.operations = operations
    }

    abstract fun customOperations(): List<CustomOperationSpec<U>>

    abstract fun getType(): Class<U>

    private fun resolveCustomOperationsSpecs(): Map<Int, CustomOperationSpec<U>> = customOperations().mapIndexed { i, op -> i to op }.toMap()

    fun applyPatch(objectMapper: ObjectMapper, body: Any): U {
        val customOperationsSpecs: Map<Int, CustomOperationSpec<U>> = resolveCustomOperationsSpecs()
        val standardCustomOperations: Map<Boolean, List<PatchOperation>> = operations.stream().map {
            normalizeStandardOperation(customOperationsSpecs, it)
        }.collect(Collectors.partitioningBy { it !is CustomPatchOperation })
        var mappedDto: U = patchDtoObject(objectMapper, body, standardCustomOperations[true] ?: emptyList())
        return standardCustomOperations[false]?.fold(mappedDto) { dto, op -> customOperationsSpecs[(op as CustomPatchOperation).customOperationKey]?.modifyFn?.apply(op, dto) ?: dto } ?: mappedDto
    }

    private fun patchDtoObject(objectMapper: ObjectMapper, body: Any, ops: List<PatchOperation>): U {
        val jsonNode = objectMapper.convertValue(body, JsonNode::class.java)
        val node: JsonNode = objectMapper.convertValue(ops, JsonNode::class.java)
        val patched = objectMapper.treeToValue(node, JsonPatch::class.java).apply(jsonNode)
        return objectMapper.treeToValue(patched, getType())
    }

    private fun normalizeStandardOperation(customOperationsSpecs: Map<Int, CustomOperationSpec<U>>, operation: PatchOperation): PatchOperation {
        return customOperationsSpecs.entries.find { (key, opSpec) -> opSpec.checkFn.test(operation) }?.let { CustomPatchOperation(operation, it.key) } ?: operation
    }
}

open class PatchOperation(val op: String, val path: String, val value: Map<String, Any>) {
}

class DefaultPatchOperation(op: String, path: String, value: Map<String, Any>) : PatchOperation(op, path, value) {
}

class CustomPatchOperation(op: PatchOperation, val customOperationKey: Int) : PatchOperation(op.op, op.path, op.value) {
}

data class CustomOperationSpec<U>(val checkFn: Predicate<PatchOperation>, val modifyFn: BiFunction<PatchOperation, U, U>)
