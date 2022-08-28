package com.angorasix.clubs.domain.club.modification

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.commons.domain.RequestingContributor

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
abstract class ClubModification<U>(modifyValue: U) : DomainObjectModification<Club, U>(modifyValue)

class AddMember(member: Member) : ClubModification<Member>(member) {
    override fun modify(updatingContributor: RequestingContributor, club: Club): Club {
        if ((!updatingContributor.isProjectAdmin).and(updatingContributor.id != modifyValue.contributorId)) throw IllegalArgumentException("Can't add this member")
        club.addMember(modifyValue)
        return club
    }
}

class RemoveMember(member: Member) : ClubModification<Member>(member) {
    override fun modify(updatingContributor: RequestingContributor, club: Club): Club {
        if ((!updatingContributor.isProjectAdmin).and(updatingContributor.id != modifyValue.contributorId)) throw IllegalArgumentException("Can't remove this member")
        club.removeMember(modifyValue)
        return club
    }
}