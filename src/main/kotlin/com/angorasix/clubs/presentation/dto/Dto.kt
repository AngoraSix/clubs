package com.angorasix.clubs.presentation.dto

import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.modification.AddMember
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.domain.club.modification.RemoveMember
import com.angorasix.clubs.infrastructure.presentation.rest.patch.PatchOperation
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.hateoas.RepresentationModel
import java.time.ZonedDateTime
import java.util.function.BiFunction
import java.util.function.Predicate

/**
 *
 *
 * @author rozagerardo
 */
data class MemberDto(
        var contributorId: String? = null,
        var roles: Collection<String> = mutableSetOf(),
        var data: Map<String, Any> = mutableMapOf()
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
        val createdAt: ZonedDateTime? = null,
) : RepresentationModel<ClubDto>()

data class ContributorHeaderDto(
        var contributorId: String,
        var attributes: Map<String, String> = mutableMapOf(),
        var projectAdmin: Boolean = false
)

data class PatchOperationSpec<U>(val checkFn: Predicate<PatchOperation>, val mapOperationFn: BiFunction<PatchOperation, ObjectMapper, ClubModification<U>>) {
    companion object {
        enum class SupportedOperations {
            REMOVE {
                override fun supportsPatchOperation(operation: PatchOperation): Boolean = operation.op == "remove" && operation.path == "/members/-"
                override fun mapToObjectModification(contributor: Member, operation: PatchOperation, objectMapper: ObjectMapper): ClubModification<Member> {
                    var memberValue = operation?.let { objectMapper.treeToValue(operation.value, Member::class.java) } ?: contributor
                    return RemoveMember(memberValue)
                }
            },
            ADD {
                override fun supportsPatchOperation(operation: PatchOperation): Boolean = operation.op == "add" && operation.path == "/members/-"
                override fun mapToObjectModification(contributor: Member, operation: PatchOperation, objectMapper: ObjectMapper): ClubModification<Member> {
                    var memberValue = operation?.let { objectMapper.treeToValue(operation.value, Member::class.java) } ?: contributor
                    return AddMember(memberValue)
                }
            };

            abstract fun supportsPatchOperation(operation: PatchOperation): Boolean
            abstract fun mapToObjectModification(contributor: Member, operation: PatchOperation, objectMapper: ObjectMapper): ClubModification<out Any>
        }
    }
}

val supportedOperations: List<PatchOperationSpec<out Any>> = listOf(PatchOperationSpec(
        { operation -> operation.op == "remove" && operation.path == "/members/-" },
        { operation, objectMapper ->
            var memberValue = objectMapper.treeToValue(operation.value, Member::class.java)
            RemoveMember(memberValue)
        }))
