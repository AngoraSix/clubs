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
            email: String,
            clubId: String,
            contributorId: String? = null,
        ): InvitationToken {
            val expirationInstant = Instant.now().plusSeconds(tokenConfigurations.expirationTime)
            val claims = JwtClaimsSet.builder()
                .issuer(tokenConfigurations.issuer)
                .subject(email)
                .issuedAt(Instant.now())
                .expiresAt(expirationInstant)
                .claim("alg", "HS256") // Specify the algorithm in claims
                .claims {
                    it.putAll(
                        mapOf(
                            TokenConfiguration.CLAIMS_CONTRIBUTOR_EMAIL to email,
                            TokenConfiguration.CLAIMS_CLUB_ID to clubId,
                            TokenConfiguration.CLAIMS_CONTRIBUTOR_ID to contributorId,
                        ),
                    )
                }
                .build()
            val header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT") // Optional
                .build()

            val tokenValue = jwtEncoder.encode(
                JwtEncoderParameters.from(
                    header,
                    claims,
                ),
            ).tokenValue
            return InvitationToken(
                email = email,
                clubId = clubId,
                tokenValue = tokenValue,
                expirationInstant = Instant.now().plusSeconds(tokenConfigurations.expirationTime),
            )
        }
    }
}
