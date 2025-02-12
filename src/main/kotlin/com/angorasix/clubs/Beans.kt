package com.angorasix.clubs

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.application.InvitationTokenService
import com.angorasix.clubs.infrastructure.persistence.converter.ZonedDateTimeConvertersUtils
import com.angorasix.clubs.infrastructure.security.ClubSecurityConfiguration
import com.angorasix.clubs.infrastructure.token.TokenConfiguration
import com.angorasix.clubs.presentation.handler.ClubHandler
import com.angorasix.clubs.presentation.router.ClubRouter
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

val beans = beans {
    bean<MongoCustomConversions> {
        MongoCustomConversions(
            listOf(
                ref<ZonedDateTimeConvertersUtils.ZonedDateTimeReaderConverter>(),
                ref<ZonedDateTimeConvertersUtils.ZonedDateTimeWritingConverter>(),
            ),
        )
    }

    bean {
        ClubSecurityConfiguration.tokenEncryptionUtils(ref())
    }
    bean {
        ClubSecurityConfiguration.springSecurityFilterChain(ref())
    }
    bean {
        InvitationTokenService(ref(),ref(),ref(),ref(),ref("invitationJwtEncoder"),ref("invitationJwtDecoder"))
    }
    bean<ClubService>()
    bean<ClubHandler>()
    bean {
        ClubRouter(ref(), ref()).clubRouterFunction()
    }
    bean("invitationJwtEncoder") {
        TokenConfiguration.jwtEncoder(ref())
    }
    bean("invitationJwtDecoder") {
        TokenConfiguration.jwtDecoder(ref())
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) = beans.initialize(context)
}
