package com.angorasix.clubs.infrastructure.security

import com.angorasix.clubs.infrastructure.config.security.SecurityConfigurations
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 *
 *
 * All Spring Security configuration.
 *
 *
 * @author rozagerardo
 */
class ClubSecurityConfiguration {

    companion object {


        fun tokenEncryptionUtils(securityConfigs: SecurityConfigurations): TokenEncryptionUtil =
            TokenEncryptionUtil(securityConfigs)

        /**
         *
         *
         * Security Filter Chain setup.
         *
         *
         * @param http Spring's customizable ServerHttpSecurity bean
         * @return fully configured SecurityWebFilterChain
         */
        fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
            http.authorizeExchange { exchanges: ServerHttpSecurity.AuthorizeExchangeSpec ->
                exchanges
                    .pathMatchers(
                        HttpMethod.GET,
                        "/clubs/**",
                    ).permitAll()
                    .anyExchange().authenticated()
            }.oauth2ResourceServer { oauth2 -> oauth2.jwt(Customizer.withDefaults()) }
            return http.build()
        }
    }
}


private const val ALG = "SHA-256"

private const val KEY_SIZE_LIMIT = 16

class TokenEncryptionUtil(private val securityConfigs: SecurityConfigurations) {

    fun encrypt(token: String): String {
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        val secretKey: SecretKey = getAesKeyFromString(securityConfigs.secretKey)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(token.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    private fun getAesKeyFromString(key: String): SecretKey {
        // Hash the key to ensure it's 16 bytes (128 bits)
        val digest = MessageDigest.getInstance(ALG).digest(key.toByteArray())
        val aesKey = digest.copyOf(KEY_SIZE_LIMIT) // Use only the first 16 bytes for AES-128
        return SecretKeySpec(aesKey, ENCRYPTION_ALGORITHM)
    }

    companion object {
        private const val ENCRYPTION_ALGORITHM = "AES"
    }
}
