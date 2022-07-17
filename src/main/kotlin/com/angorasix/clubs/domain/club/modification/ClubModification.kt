package com.angorasix.clubs.domain.club.modification

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member


/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface DomainObjectModification<T> {
    open fun modify(updatingContributor: Member, domainObject: T): T;
}

abstract class ClubModification<U>(val modifyValue: U) : DomainObjectModification<Club>

class AddMember(member: Member) : ClubModification<Member>(member) {
    override fun modify(updatingContributor: Member, club: Club): Club {
        if ((!updatingContributor.isProjectAdmin).and(updatingContributor != modifyValue)) throw IllegalArgumentException("Can't add this member")
        club.addMember(modifyValue)
        return club
    }
}

class RemoveMember(member: Member) : ClubModification<Member>(member) {
    override fun modify(updatingContributor: Member, club: Club): Club {
        if ((!updatingContributor.isProjectAdmin).and(updatingContributor != modifyValue)) throw IllegalArgumentException("Can't remove this member")
        club.removeMember(modifyValue)
        return club
    }
}