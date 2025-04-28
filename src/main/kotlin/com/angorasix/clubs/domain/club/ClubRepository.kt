package com.angorasix.clubs.domain.club

import com.angorasix.clubs.infrastructure.persistence.repository.ClubFilterRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ClubRepository :
    CoroutineCrudRepository<Club, String>,
    CoroutineSortingRepository<Club, String>,
    ClubFilterRepository
