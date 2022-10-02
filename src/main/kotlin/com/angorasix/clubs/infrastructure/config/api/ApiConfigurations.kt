package com.angorasix.clubs.infrastructure.config.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@Configuration
@ConfigurationProperties(prefix = "configs.api")
class ApiConfigs {
    lateinit var headers: HeadersConfigs
    lateinit var routes: RoutesConfigs
    lateinit var basePaths: BasePathConfigs
    lateinit var clubActions: ClubActions
}

class HeadersConfigs @ConstructorBinding constructor(val contributor: String)

class BasePathConfigs @ConstructorBinding constructor(val clubs: String, val wellKnown: String)

class RoutesConfigs @ConstructorBinding constructor(
    val wellKnownGetSingle: Route,
    val wellKnownGetAll: Route,
    val wellKnownPatch: Route,
)

data class Route(
    val name: String,
    val basePaths: List<String>,
    val method: HttpMethod,
    val path: String,
) {
    fun resolvePath(): String = basePaths.joinToString("").plus(path)
}

class ClubActions @ConstructorBinding constructor(val addMember: String, val removeMember: String)
