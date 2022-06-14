package com.angorasix.clubs.domain.club

import com.angorasix.clubs.infrastructure.persistence.repository.ClubFilterRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface ClubRepository : CoroutineCrudRepository<Club, String>,
        CoroutineSortingRepository<Club, String>,
        ClubFilterRepository {
    suspend fun findByTypeAndProjectId(type: String, projectId: String?): Club?
}


