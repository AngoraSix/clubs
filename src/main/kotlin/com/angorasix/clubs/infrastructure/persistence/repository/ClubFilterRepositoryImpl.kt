package com.angorasix.clubs.infrastructure.persistence.repository

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.commons.domain.SimpleContributor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ClubFilterRepositoryImpl(val mongoOps: ReactiveMongoOperations) : ClubFilterRepository {

    override fun findUsingFilter(
        filter: ListClubsFilter,
        requestingContributor: SimpleContributor?,
    ): Flow<Club> {
        return mongoOps.find(filter.toQuery(requestingContributor), Club::class.java).asFlow()
    }
}

private fun ListClubsFilter.toQuery(requestingContributor: SimpleContributor?): Query {
    val query = Query()

    projectId?.let { query.addCriteria(where("projectId").`in`(it)) }
    type?.let { query.addCriteria(where("type").`is`(it)) }
    query.addCriteria(
        Criteria().orOperator(
            where("admins").elemMatch(
                where("contributorId").`is`(
                    requestingContributor?.contributorId,
                ),
            ),
            where("open").`is`(true),
            where("public").`is`(true),
            where("members").elemMatch(where("contributorId").`in`(memberContributorId)),
        ),
    )
    adminId?.let {
        query.addCriteria(
            where("admins").elemMatch(
                where("contributorId").`in`(
                    it
                ),
            ),
        )
    }
    return query
}
