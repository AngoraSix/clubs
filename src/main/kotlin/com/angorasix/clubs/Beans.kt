package com.angorasix.clubs

import com.angorasix.clubs.application.ClubService
import com.angorasix.clubs.infrastructure.persistence.converter.ZonedDateTimeConvertersUtils
import com.angorasix.clubs.presentation.handler.ClubHandler
import com.angorasix.clubs.presentation.router.ClubRouter
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.web.server.adapter.ForwardedHeaderTransformer

val beans = beans {
    bean<MongoCustomConversions> {
        MongoCustomConversions(
            listOf(
                ref<ZonedDateTimeConvertersUtils.ZonedDateTimeReaderConverter>(),
                ref<ZonedDateTimeConvertersUtils.ZonedDateTimeWritingConverter>()
            )
        )
    }
    bean<ClubService>()
    bean<ClubHandler>()
    bean {
        ClubRouter(ref(), ref(), ref()).clubRouterFunction()
    }
    bean {
        ForwardedHeaderTransformer()
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) = beans.initialize(context)
}
