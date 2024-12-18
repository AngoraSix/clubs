package com.angorasix.clubs.infrastructure.token

import com.angorasix.clubs.infrastructure.config.token.TokenConfigurations
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import java.time.Instant

class InvitationTokenUtils {
    companion object {
        fun createInvitationToken(
            jwtEncoder: JwtEncoder,
            tokenConfigurations: TokenConfigurations,
            contributorId: String,
            clubId: String,
        ): String {
            val claims = JwtClaimsSet.builder()
                .issuer(tokenConfigurations.issuer)
                .subject(contributorId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(tokenConfigurations.expirationTime))
                .claim("alg", "HS256") // Specify the algorithm in claims
                .claims {
                    it.putAll(
                        mapOf(
                            TokenConfiguration.CLAIMS_CONTRIBUTOR_ID to contributorId,
                            TokenConfiguration.CLAIMS_CLUB_ID to clubId,
                        ),
                    )
                }
                .build()

            val header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT") // Optional
                .build()

            return jwtEncoder.encode(
                JwtEncoderParameters.from(
                    header,
                    claims,
                ),
            ).tokenValue
        }
    }
}
