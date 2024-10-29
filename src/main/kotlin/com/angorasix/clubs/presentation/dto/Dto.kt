package com.angorasix.clubs.presentation.dto

import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.domain.club.modification.AddMember
import com.angorasix.clubs.domain.club.modification.ClubModification
import com.angorasix.clubs.domain.club.modification.RemoveMember
import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.presentation.dto.PatchOperation
import com.angorasix.commons.presentation.dto.PatchOperationSpec
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.ZonedDateTime

/**
 *
 *
 * @author rozagerardo
 */
data class MemberDto(
    var contributorId: String? = null,
    var roles: Collection<String> = mutableSetOf(),
    var data: Map<String, Any> = mutableMapOf(),
)

@Relation(collectionRelation = "clubList", itemRelation = "club")
data class ClubDto(
    val id: String? = null,
    val name: String? = null,
    val type: String,
    val description: String? = null,
    val projectId: String? = null,
    val members: MutableSet<MemberDto> = mutableSetOf(),
    val admins: Set<SimpleContributor> = emptySet(),
    val open: Boolean? = null,
    val public: Boolean? = null,
    val social: Boolean? = null,
    val createdAt: ZonedDateTime? = null,
) : RepresentationModel<ClubDto>()

enum class SupportedPatchOperations(val op: PatchOperationSpec) {
    REMOVE(
        object : PatchOperationSpec {
            override fun supportsPatchOperation(operation: PatchOperation): Boolean =
                operation.op == "remove" && operation.path == "/members/-"

            override fun mapToObjectModification(
                contributor: SimpleContributor,
                operation: PatchOperation,
                objectMapper: ObjectMapper,
            ): ClubModification<Member> {
                var memberValue = objectMapper.treeToValue(operation.value, Member::class.java)
                    ?: Member(contributor.contributorId, emptyList(), emptyMap())
                return RemoveMember(memberValue)
            }
        },
    ),
    ADD(
        object : PatchOperationSpec {
            override fun supportsPatchOperation(operation: PatchOperation): Boolean =
                operation.op == "add" && operation.path == "/members/+"

            override fun mapToObjectModification(
                contributor: SimpleContributor,
                operation: PatchOperation,
                objectMapper: ObjectMapper,
            ): ClubModification<Member> {
                var memberValue = objectMapper.treeToValue(operation.value, Member::class.java)
                    ?: Member(contributor.contributorId, emptyList(), emptyMap())
                return AddMember(memberValue)
            }
        },
    ),
}
