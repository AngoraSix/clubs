package com.angorasix.clubs.messaging.listener.router

import com.angorasix.clubs.messaging.listener.handler.MessagingHandler
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.dto.project.ProjectCreated
import com.angorasix.commons.infrastructure.intercommunication.dto.projectmanagement.ProjectManagementCreated
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
    // Revert this when this is GA: https://github.com/spring-cloud/spring-cloud-function/issues/124
    @Bean
    fun registerWellKnownClubsForProject(): java.util.function.Function<A6InfraMessageDto<ProjectCreated>, Unit> =
        java.util.function.Function { handler.processProjectCreated(it) }

    @Bean
    fun registerWellKnownClubsForProjectManagement(): java.util.function.Function<A6InfraMessageDto<ProjectManagementCreated>, Unit> =
        java.util.function.Function { handler.processProjectManagementCreated(it) }
}
