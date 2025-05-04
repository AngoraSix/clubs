package com.angorasix.clubs.infrastructure.persistence.repository

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.domain.club.Member
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.commons.domain.A6Contributor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ClubFilterRepositoryImpl(
    private val mongoOps: ReactiveMongoOperations,
) : ClubFilterRepository {
    override fun findUsingFilter(
        filter: ListClubsFilter,
        requestingContributor: A6Contributor?,
    ): Flow<Club> = mongoOps.find(filter.toQuery(requestingContributor), Club::class.java).asFlow()

    override suspend fun findSingleUsingFilter(
        filter: ListClubsFilter,
        requestingContributor: A6Contributor?,
    ): Club? = mongoOps.find(filter.toQuery(requestingContributor), Club::class.java).awaitFirstOrNull()

    override suspend fun addMemberToClub(
        clubId: String,
        member: Member,
        requestingContributor: A6Contributor,
        fromInvitation: Boolean,
    ): Club? {
        // check if is already member
        val query =
            addMemberConditionsQuery(
                clubId,
                fromInvitation,
                member.contributorId,
                requestingContributor,
            )
        val update = Update()
        update.addToSet("members", member)
        return mongoOps.findAndModify(query, update, Club::class.java).awaitFirstOrNull()
    }
}

private fun ListClubsFilter.toQuery(requestingContributor: A6Contributor?): Query {
    val query = Query()

    projectId?.let { query.addCriteria(where("projectId").`in`(it as Collection<Any>)) }
    projectManagementId?.let { query.addCriteria(where("projectManagementId").`in`(it as Collection<Any>)) }
    type?.let { query.addCriteria(where("type").`is`(it)) }
    val orCriteria =
        mutableListOf(
            where("admins").elemMatch(
                where("contributorId").`is`(requestingContributor?.contributorId),
            ),
            where("open").`is`(true),
            where("public").`is`(true),
        )
    if (!memberContributorId.isNullOrEmpty()) {
        orCriteria.add(
            where("members").elemMatch(
                where("contributorId").`in`(memberContributorId as Collection<Any>),
            ),
        )
    }
    query.addCriteria(Criteria().orOperator(orCriteria))

    adminId?.let {
        query.addCriteria(
            where("admins").elemMatch(
                where("contributorId").`in`(it as Collection<Any>),
            ),
        )
    }
    return query
}

private fun addMemberConditionsQuery(
    clubId: String,
    fromInvitation: Boolean,
    newMemberContributorId: String,
    requestingContributor: A6Contributor,
): Query {
    val query = Query()

    query.addCriteria(where("id").`is`(clubId))

    query.addCriteria(where("members.contributorId").ne(newMemberContributorId))

    if (!fromInvitation) {
        query.addCriteria(
            Criteria().orOperator(
                where("admins").elemMatch(
                    where("contributorId").`is`(
                        requestingContributor.contributorId,
                    ),
                ),
                where("open").`is`(true),
            ),
        )
    }
    return query
}
