package com.angorasix.clubs.domain.club.modification

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.domain.modification.DomainObjectModification

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
abstract class ClubModification<U>(modifyValue: U) : DomainObjectModification<Club, U>(modifyValue)

class AddMember(member: Member) : ClubModification<Member>(member) {
    override fun modify(simpleContributor: SimpleContributor, domainObject: Club): Club {
        require(
            (domainObject.isAdmin(simpleContributor.contributorId))
                .or(simpleContributor.contributorId == modifyValue.contributorId),
        ) { "Can't add this member" }
        domainObject.addMember(modifyValue)
        return domainObject
    }
}

class RemoveMember(member: Member) : ClubModification<Member>(member) {
    override fun modify(requestingContributor: SimpleContributor, domainObject: Club): Club {
        require(
            (domainObject.isAdmin(requestingContributor.contributorId))
                .or(requestingContributor.contributorId == modifyValue.contributorId),
        ) { "Can't remove this member" }
        domainObject.removeMember(modifyValue)
        return domainObject
    }
}
