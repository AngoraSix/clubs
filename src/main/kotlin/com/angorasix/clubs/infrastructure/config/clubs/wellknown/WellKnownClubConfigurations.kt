package com.angorasix.clubs.infrastructure.config.clubs.wellknown

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "wellknown.configurations.clubs")
class WellKnownClubConfigurations {

    var wellKnownClubTypes: Map<String, String>
    var wellKnownClubDescriptions: MutableMap<String, WellKnownClubDescription>

    constructor(
        wellKnownClubTypes: Map<String, String>,
        wellKnownClubDescriptions: Collection<RawWellKnownClubDescription>,
    ) {
        this.wellKnownClubTypes = wellKnownClubTypes
        this.wellKnownClubDescriptions =
            processProperties(wellKnownClubTypes, wellKnownClubDescriptions)
    }
}

fun processProperties(
    wellKnownClubTypes: Map<String, String>,
    wellKnownClubDescriptions: Collection<RawWellKnownClubDescription>,
): MutableMap<String, WellKnownClubDescription> {
    val typeToRequirement: MutableMap<String, Class<out DescriptionRequirements>> =
        mutableMapOf(wellKnownClubTypes["contributorCandidates"]!! to (ContributorCandidatesRequirements::class.java))
    return wellKnownClubDescriptions.map {
        WellKnownClubDescription(
            it,
            typeToRequirement[it.type]!!,
        )
    }
        .associateBy {
            it.type
        } as MutableMap<String, WellKnownClubDescription>
}

class WellKnownClubDescription constructor(
    rawDescription: RawWellKnownClubDescription,
    mappedRequirements: Class<out DescriptionRequirements>,
) {
    var type: String = rawDescription.type
    var open: Boolean = rawDescription.open
    var public: Boolean = rawDescription.public
    var social: Boolean = rawDescription.social
    var requirements: Class<out DescriptionRequirements> = mappedRequirements
}

data class RawWellKnownClubDescription(
    var type: String,
    var open: Boolean,
    var public: Boolean,
    var social: Boolean,
)

interface DescriptionRequirements

data class ContributorCandidatesRequirements(var contact: String) : DescriptionRequirements

data class AdminContributorRequirements(var isAdmin: Boolean) : DescriptionRequirements
