package com.angorasix.clubs.infrastructure.token

import com.angorasix.clubs.infrastructure.config.token.TokenConfigurations
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwtException
import java.time.Instant
import kotlin.math.exp

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
                contributorId = contributorId,
            )
        }

        /**
         * Decode and validate the JWT string. Return a custom InvitationToken if valid,
         * or null/throw if invalid or expired.
         */
        fun decodeToken(tokenValue: String, jwtDecoder: JwtDecoder): InvitationToken? {
            return try {
                val jwt: Jwt = jwtDecoder.decode(tokenValue)
                // e.g., check custom claims, expiration, etc.
                val email = jwt.claims[TokenConfiguration.CLAIMS_CONTRIBUTOR_EMAIL] as? String ?: return null
                val clubId = jwt.claims[TokenConfiguration.CLAIMS_CLUB_ID] as? String ?: return null
                val contributorId = jwt.claims[TokenConfiguration.CLAIMS_CONTRIBUTOR_ID] as? String?

                // Could check expiration if desired (though decode()
                // normally throws on expired tokens). For extra caution:
                val expiresAt = jwt.expiresAt

                println(expiresAt)
                if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
                    return null
                }

                InvitationToken(
                    email = email,
                    clubId = clubId,
                    tokenValue = tokenValue,
                    expirationInstant = expiresAt,
                    contributorId = contributorId,
                )
            } catch (ex: JwtException) {
                // invalid signature, malformed token, or expired
                null
            }
        }
    }
}
