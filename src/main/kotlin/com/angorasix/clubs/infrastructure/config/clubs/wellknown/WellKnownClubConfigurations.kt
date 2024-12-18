package com.angorasix.clubs.infrastructure.config.clubs.wellknown

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "wellknown.configurations.clubs")
class WellKnownClubConfigurations(
    var wellKnownClubTypes: Map<WellKnownClubTypes, String>,
    wellKnownClubDescriptions: Collection<RawWellKnownClubDescription>
) {

    var wellKnownClubDescriptions: MutableMap<String, WellKnownClubDescription> =
        processProperties(wellKnownClubTypes, wellKnownClubDescriptions)
}

fun processProperties(
    wellKnownClubTypes: Map<WellKnownClubTypes, String>,
    wellKnownClubDescriptions: Collection<RawWellKnownClubDescription>,
): MutableMap<String, WellKnownClubDescription> {
    val typeToRequirement: Map<String, Class<out DescriptionRequirements>> =
        mapOf(
            wellKnownClubTypes[WellKnownClubTypes.CONTRIBUTOR_CANDIDATES]!!  to (ContributorCandidatesRequirements::class.java),
            )
    return wellKnownClubDescriptions.map {
        WellKnownClubDescription(
            it,
            typeToRequirement[it.type],
        )
    }
        .associateBy {
            it.type
        } as MutableMap<String, WellKnownClubDescription>
}

class WellKnownClubDescription(
    rawDescription: RawWellKnownClubDescription,
    mappedRequirements: Class<out DescriptionRequirements>? = null,
) {
    var type: String = rawDescription.type
    var open: Boolean = rawDescription.open
    var public: Boolean = rawDescription.public
    var social: Boolean = rawDescription.social
    var requirements: Class<out DescriptionRequirements>? = mappedRequirements
}

data class RawWellKnownClubDescription(
    var type: String,
    var open: Boolean,
    var public: Boolean,
    var social: Boolean,
)

enum class WellKnownClubTypes{
    CONTRIBUTOR_CANDIDATES,
    PROJECT_MANAGEMENT_MEMBERS,
}

interface DescriptionRequirements

data class ContributorCandidatesRequirements(var contact: String) : DescriptionRequirements

data class AdminContributorRequirements(var isAdmin: Boolean) : DescriptionRequirements
