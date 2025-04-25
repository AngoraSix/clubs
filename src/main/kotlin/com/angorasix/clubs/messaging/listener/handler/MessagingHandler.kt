package com.angorasix.clubs.messaging.listener.handler

import com.angorasix.clubs.application.ClubService
import com.angorasix.commons.infrastructure.intercommunication.dto.A6DomainResource
import com.angorasix.commons.infrastructure.intercommunication.dto.A6InfraTopics
import com.angorasix.commons.infrastructure.intercommunication.dto.messaging.A6InfraMessageDto
import com.angorasix.commons.infrastructure.intercommunication.dto.project.ProjectCreated
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking

class MessagingHandler(
    private val service: ClubService,
    private val objectMapper: ObjectMapper,
) {
    fun processProjectCreated(message: A6InfraMessageDto<ProjectCreated>) =
        runBlocking {
            if (message.topic == A6InfraTopics.PROJECT_CREATED.value &&
                message.targetType == A6DomainResource.Project
            ) {
                val projectCreated = message.messageData
                service.registerAllWellKnownClub(
                    projectId = projectCreated.projectId,
                    requestingContributor = projectCreated.creatorContributor,
                )
            }
        }
}
