package com.angorasix.clubs.infrastructure.token

import com.angorasix.clubs.infrastructure.config.token.TokenConfigurations
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec

private const val SECRET_LENGTH_LIMIT = 32

object TokenConfiguration {
    const val CLAIMS_CONTRIBUTOR_EMAIL = "contributorEmail"
    const val CLAIMS_CLUB_ID = "clubId"
    const val CLAIMS_CONTRIBUTOR_ID = "contributorId"

    private const val TOKEN_KEY_ID = "clubTokenJWKId"

    fun jwtEncoder(tokenConfigurations: TokenConfigurations): JwtEncoder {
        require(tokenConfigurations.secret.length >= SECRET_LENGTH_LIMIT) {
            "The secret must be at least 32 characters long for HS256."
        }
        val jwk =
            OctetSequenceKey
                .Builder(tokenConfigurations.secret.toByteArray())
                .algorithm(JWSAlgorithm.HS256)
                .keyID(TOKEN_KEY_ID)
                .build()
        val jwkSource =
            JWKSource<SecurityContext> { jwkSelector, _ ->
                jwkSelector.select(JWKSet(jwk))
            }
        return NimbusJwtEncoder(jwkSource)
    }

    fun jwtDecoder(tokenConfigurations: TokenConfigurations): JwtDecoder {
        require(tokenConfigurations.secret.length >= SECRET_LENGTH_LIMIT) {
            "The secret must be at least 32 characters long for HS256."
        }
        val keySpec =
            SecretKeySpec(tokenConfigurations.secret.toByteArray(), JWSAlgorithm.HS256.name)
        return NimbusJwtDecoder.withSecretKey(keySpec).build()
    }
}
