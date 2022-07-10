package com.angorasix.clubs.infrastructure.presentation.rest.patch

import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.presentation.dto.ClubDto
import com.angorasix.clubs.presentation.dto.MemberDto
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

class ClubJsonPatch {

    @get:JsonValue
    private val operations: List<PatchOperation>

    @JsonCreator
    constructor(operations: List<DefaultPatchOperation>) {
        this.operations = operations
    }

    fun applyPatch(objectMapper: ObjectMapper, body: Any): ClubDto {
        val standardCustomOperations: Map<Boolean, List<PatchOperation>> = operations.stream().map(
                ::normalizeStandardOperation
        ).collect(Collectors.partitioningBy { it !is CustomPatchOperation })
        var mappedDto: ClubDto = patchObject(objectMapper, body, standardCustomOperations[true] ?: emptyList())
        return standardCustomOperations[false]?.fold(mappedDto) { dto, op -> CUSTOM_OPERATIONS[(op as CustomPatchOperation).customOperationKey]?.modifyFn?.apply(op, dto) ?: dto } ?: mappedDto
    }

    private inline fun <reified U : Any> patchObject(objectMapper: ObjectMapper, body: Any, ops: List<PatchOperation>): U {
        val jsonNode = objectMapper.convertValue(body, JsonNode::class.java)
        val node: JsonNode = objectMapper.convertValue(ops, JsonNode::class.java)
        val patched = objectMapper.treeToValue(node, JsonPatch::class.java).apply(jsonNode)
        return objectMapper.treeToValue(patched, U::class.java)
    }

    companion object {
        val CUSTOM_OPERATIONS: Map<String, CustomOperationSpec<ClubDto>> = mapOf("removeMember" to CustomOperationSpec(
                { op -> op.op == "remove" && op.path == "/members/-" },
                { op, clubDto ->
                    clubDto.members.removeIf{m -> m.contributorId == op.value["contributorId"]}
                    clubDto
                }))

        fun normalizeStandardOperation(operation: PatchOperation): PatchOperation {
            return CUSTOM_OPERATIONS.entries.find { (key, opSpec) -> opSpec.checkFn.test(operation) }?.let { CustomPatchOperation(operation, it.key) } ?: operation
        }


    }


}

open class PatchOperation(val op: String, val path: String, val value: Map<String, Any>) {
}

class DefaultPatchOperation(op: String, path: String, value: Map<String, Any>): PatchOperation(op, path, value) {
}

class CustomPatchOperation(op: PatchOperation, val customOperationKey: String) : PatchOperation(op.op, op.path, op.value) {
}

data class CustomOperationSpec<U>(val checkFn: Predicate<PatchOperation>, val modifyFn: BiFunction<PatchOperation, U, U>)
