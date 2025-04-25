package com.angorasix.clubs.messaging.listener.router

import com.angorasix.clubs.messaging.listener.handler.MessagingHandler
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.dto.project.ProjectCreated
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@Configuration
class MessagingRouter(
    val handler: MessagingHandler,
) {
    @Bean
    fun registerWellKnownClubsForProject(): (A6InfraMessageDto<ProjectCreated>) -> Unit = { handler.processProjectCreated(it) }
}
