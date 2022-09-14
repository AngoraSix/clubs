package com.angorasix.clubs.domain.club.modification

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.commons.domain.RequestingContributor
import com.angorasix.commons.domain.modification.DomainObjectModification

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
abstract class ClubModification<U>(modifyValue: U) : DomainObjectModification<Club, U>(modifyValue)

class AddMember(member: Member) : ClubModification<Member>(member) {
    override fun modify(requestingContributor: RequestingContributor, domainObject: Club): Club {
        require((!requestingContributor.isProjectAdmin).and(requestingContributor.id != modifyValue.contributorId))
        { "Can't add this member" }
        domainObject.addMember(modifyValue)
        return domainObject
    }
}

class RemoveMember(member: Member) : ClubModification<Member>(member) {
    override fun modify(requestingContributor: RequestingContributor, domainObject: Club): Club {
        require((!requestingContributor.isProjectAdmin).and(requestingContributor.id != modifyValue.contributorId))
        { "Can't remove this member" }
        domainObject.removeMember(modifyValue)
        return domainObject
    }
}
