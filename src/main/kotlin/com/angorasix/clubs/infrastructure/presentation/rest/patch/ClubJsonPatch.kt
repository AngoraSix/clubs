package com.angorasix.clubs.infrastructure.presentation.rest.patch

import com.angorasix.clubs.presentation.dto.ClubDto


/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */

class ClubJsonPatch(operations: List<DefaultPatchOperation>) : DtoObjectPatch<ClubDto>(operations) {
    override fun customOperations(): List<CustomOperationSpec<ClubDto>> = listOf(CustomOperationSpec(
            { op -> op.op == "remove" && op.path == "/members/-" },
            { op, clubDto ->
                clubDto.members.removeIf { m -> m.contributorId == op.value["contributorId"] }
                clubDto
            }))

    override fun getType(): Class<ClubDto> = ClubDto::class.java
}