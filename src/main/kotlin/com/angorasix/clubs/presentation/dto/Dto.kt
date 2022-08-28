package com.angorasix.clubs.presentation.dto

import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.modification.AddMember
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.domain.club.modification.RemoveMember
import com.angorasix.clubs.infrastructure.presentation.rest.patch.PatchOperation
import com.angorasix.clubs.infrastructure.presentation.rest.patch.PatchOperationSpec
import com.angorasix.commons.domain.RequestingContributor
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.hateoas.RepresentationModel
import java.time.ZonedDateTime

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

enum class SupportedPatchOperations(val op: PatchOperationSpec) {
    REMOVE(object : PatchOperationSpec() {
        override fun supportsPatchOperation(operation: PatchOperation): Boolean = operation.op == "remove" && operation.path == "/members/-"
        override fun mapToObjectModification(requestingContributor: RequestingContributor, operation: PatchOperation, objectMapper: ObjectMapper): ClubModification<Member> {//DomainObjectModification<out Any, out Any> {
            var memberValue = operation?.let { objectMapper.treeToValue(operation.value, Member::class.java) } ?: Member(requestingContributor.id, emptyList(), emptyMap(), requestingContributor.isProjectAdmin)
            return RemoveMember(memberValue)
        }
    }),
    ADD(object : PatchOperationSpec() {
        override fun supportsPatchOperation(operation: PatchOperation): Boolean = operation.op == "add" && operation.path == "/members/-"
        override fun mapToObjectModification(requestingContributor: RequestingContributor, operation: PatchOperation, objectMapper: ObjectMapper): ClubModification<Member> {
            var memberValue = operation?.let { objectMapper.treeToValue(operation.value, Member::class.java) } ?: Member(requestingContributor.id, emptyList(), emptyMap(), requestingContributor.isProjectAdmin)
            return AddMember(memberValue)
        }
    });
}

//val supportedOperations: List<PatchOperationSpec<out Any>> = listOf(PatchOperationSpec(
//        { operation -> operation.op == "remove" && operation.path == "/members/-" },
//        { operation, objectMapper ->
//            var memberValue = objectMapper.treeToValue(operation.value, Member::class.java)
//            RemoveMember(memberValue)
//        }))
