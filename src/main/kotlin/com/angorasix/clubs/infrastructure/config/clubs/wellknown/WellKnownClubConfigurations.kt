package com.angorasix.clubs.infrastructure.config.clubs.wellknown

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@Configuration
@ConfigurationProperties(prefix = "configs")
class WellKnownClubConfigurations {
    lateinit var clubs: Clubs

}

class Clubs @ConstructorBinding constructor(wellKnownClubTypes: Map<String, String>, wellKnownClubDescriptions: Collection<RawWellKnownClubDescription>) {
    private val typeToRequirement: MutableMap<String, Class<out DescriptionRequirements>> = mutableMapOf(wellKnownClubTypes["contributorCandidates"]!! to (ContributorCandidatesRequirements::class.java))

    var wellKnownClubTypes = wellKnownClubTypes
    var wellKnownClubDescriptions: MutableMap<String, WellKnownClubDescription> = wellKnownClubDescriptions.map { WellKnownClubDescription(it, typeToRequirement[it.type]!! ) }.associateBy {
        it.type
    } as MutableMap<String, WellKnownClubDescription>

}

class WellKnownClubDescription constructor(
        rawDescription: RawWellKnownClubDescription,
        mappedRequirements: Class<out DescriptionRequirements>
) {
    var type: String
    var description: String
    var open: Boolean
    var public: Boolean
    var social: Boolean
    var requirements: Class<out DescriptionRequirements>

    init {
        type = rawDescription.type
        description = rawDescription.description
        open = rawDescription.open
        public = rawDescription.public
        social = rawDescription.social
        requirements = mappedRequirements
    }
}

class RawWellKnownClubDescription constructor(
        var type: String,
        var description: String,
        var open: Boolean,
        var public: Boolean,
        var social: Boolean,
)

interface DescriptionRequirements

class ContributorCandidatesRequirements(var contact: String) : DescriptionRequirements
