package com.angorasix.clubs.infrastructure.persistence.repository

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
import com.angorasix.commons.domain.RequestingContributor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoOperations
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
        requestingContributor: RequestingContributor?,
    ): Flow<Club> {
        return mongoOps.find(filter.toQuery(requestingContributor), Club::class.java).asFlow()
    }
}

private fun ListClubsFilter.toQuery(requestingContributor: RequestingContributor?): Query {
    val query = Query()

    val requestingOwn = requestingContributor?.isProjectAdmin ?: false

    projectId?.let { query.addCriteria(where("projectId").`in`(it)) }
    contributorId?.let { query.addCriteria(where("members").elemMatch(where("contributorId").`is`(it))) }
    type?.let { query.addCriteria(where("type").`is`(it)) }

    if (!requestingOwn) {
        query.addCriteria(where("public").`is`(true))
    }
    return query
}
