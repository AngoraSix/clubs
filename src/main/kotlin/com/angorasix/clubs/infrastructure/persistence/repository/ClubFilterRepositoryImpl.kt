package com.angorasix.clubs.infrastructure.persistence.repository

import com.angorasix.clubs.domain.club.Club
import com.angorasix.clubs.infrastructure.queryfilters.ListClubsFilter
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

    override fun findUsingFilter(filter: ListClubsFilter): Flow<Club> {
        return mongoOps.find(filter.toQuery(), Club::class.java).asFlow()
    }
}

private fun ListClubsFilter.toQuery(): Query {
    val query = Query()
    projectId?.let { query.addCriteria(where("projectId").`in`(it)) }
    contributorId?.let { query.addCriteria(where("members").elemMatch(where("contributorId").`is`(it))) }
    type?.let { query.addCriteria(where("type").`is`(it)) }
    return query
}
