package com.angorasix.clubs.infrastructure.config.clubs.wellknown

import com.angorasix.commons.domain.clubs.WellKnownClubTypes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "wellknown.configurations.clubs")
class WellKnownClubConfigurations(
    wellKnownClubTypes: Map<WellKnownClubTypes, WellKnownClubTypes>, // to validate correct usage of values...
    wellKnownClubDescriptions: Collection<RawWellKnownClubDescription>,
) {
    // default
    private val logger: Logger =
        LoggerFactory.getLogger(
            WellKnownClubConfigurations::class.java,
        )

    init {
        logger.isDebugEnabled.let { logger.debug("Supported WellKnownClubTypes: {}", wellKnownClubTypes) }
    }

    var wellKnownClubDescriptions: MutableMap<String, WellKnownClubDescription> =
        processProperties(wellKnownClubDescriptions)
}

fun processProperties(wellKnownClubDescriptions: Collection<RawWellKnownClubDescription>): MutableMap<String, WellKnownClubDescription> {
    val typeToRequirement: Map<String, Class<out DescriptionRequirements>> =
        mapOf(
            WellKnownClubTypes.CONTRIBUTOR_CANDIDATES.name
                to (ContributorCandidatesRequirements::class.java),
        )
    return wellKnownClubDescriptions
        .map {
            WellKnownClubDescription(
                it,
                typeToRequirement[it.type],
            )
        }.associateBy {
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
    var isCreatorMember: Boolean = rawDescription.isCreatorMember
    var isProjectClub: Boolean = rawDescription.isProjectClub
    var isProjectManagementClub: Boolean = rawDescription.isProjectManagementClub
    var requirements: Class<out DescriptionRequirements>? = mappedRequirements
}

data class RawWellKnownClubDescription(
    var type: String,
    var open: Boolean,
    var public: Boolean,
    var social: Boolean,
    var isCreatorMember: Boolean = false,
    var isProjectClub: Boolean = false,
    var isProjectManagementClub: Boolean = false,
)

interface DescriptionRequirements

data class ContributorCandidatesRequirements(
    var contact: String,
) : DescriptionRequirements

data class AdminContributorRequirements(
    var isAdmin: Boolean,
) : DescriptionRequirements
