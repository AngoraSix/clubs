package com.angorasix.clubs.infrastructure.service

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.application.InvitationTokenService
import com.angorasix.clubs.domain.club.ClubRepository
import com.angorasix.clubs.infrastructure.config.amqp.AmqpConfigurations
import com.angorasix.clubs.infrastructure.config.api.ApiConfigs
import com.angorasix.clubs.infrastructure.config.clubs.wellknown.WellKnownClubConfigurations
import com.angorasix.clubs.infrastructure.config.token.TokenConfigurations
import com.angorasix.clubs.infrastructure.security.TokenEncryptionUtil
import com.angorasix.clubs.infrastructure.token.TokenConfiguration
import com.angorasix.clubs.messaging.publisher.MessagePublisher
import com.angorasix.clubs.presentation.handler.InvitationsHandler
import com.angorasix.clubs.presentation.handler.WellKnownClubHandler
import com.angorasix.clubs.presentation.router.ClubRouter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder

@Configuration
class ServiceConfiguration {
    @Bean("invitationTokenJwtEncoder")
    fun invitationJwtEncoder(tokenConfigurations: TokenConfigurations): JwtEncoder = TokenConfiguration.jwtEncoder(tokenConfigurations)

    @Bean("invitationTokenJwtDecoder")
    fun invitationJwtDecoder(tokenConfigurations: TokenConfigurations): JwtDecoder = TokenConfiguration.jwtDecoder(tokenConfigurations)

    @Bean
    fun invitationTokenService(
        repository: ClubRepository,
        messagePublisher: MessagePublisher,
        tokenConfigurations: TokenConfigurations,
        @Qualifier("invitationTokenJwtEncoder") invitationTokenJwtEncoder: JwtEncoder,
        @Qualifier("invitationTokenJwtDecoder") invitationTokenJwtDecoder: JwtDecoder,
    ) = InvitationTokenService(repository, messagePublisher, tokenConfigurations, invitationTokenJwtEncoder, invitationTokenJwtDecoder)

    @Bean
    fun clubService(
        repository: ClubRepository,
        invitationTokenService: InvitationTokenService,
        encryptionUtils: TokenEncryptionUtil,
        wellKnownClubConfigurations: WellKnownClubConfigurations,
    ) = ClubService(repository, invitationTokenService, encryptionUtils, wellKnownClubConfigurations)

    @Bean
    fun wellKnownClubHandler(
        service: ClubService,
        apiConfigs: ApiConfigs,
        wellKnownClubConfigurations: WellKnownClubConfigurations,
        objectMapper: ObjectMapper,
    ) = WellKnownClubHandler(service, apiConfigs, wellKnownClubConfigurations, objectMapper)

    @Bean
    fun invitationsHandler(
        service: ClubService,
        invitationTokenService: InvitationTokenService,
        apiConfigs: ApiConfigs,
        wellKnownClubConfigurations: WellKnownClubConfigurations,
    ) = InvitationsHandler(service, invitationTokenService, apiConfigs, wellKnownClubConfigurations)

    @Bean
    fun clubRouter(
        wellKnownClubHandler: WellKnownClubHandler,
        invitationsHandler: InvitationsHandler,
        apiConfigs: ApiConfigs,
    ) = ClubRouter(wellKnownClubHandler, invitationsHandler, apiConfigs).clubRouterFunction()

    @Bean
    fun messagePublisher(
        streamBridge: StreamBridge,
        amqpConfigs: AmqpConfigurations,
    ) = MessagePublisher(streamBridge, amqpConfigs)
}
