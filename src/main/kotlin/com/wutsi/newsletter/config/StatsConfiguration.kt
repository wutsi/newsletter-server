package com.wutsi.newsletter.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.security.apikey.ApiKeyRequestInterceptor
import com.wutsi.stats.StatsApi
import com.wutsi.stats.StatsApiBuilder
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
public class StatsConfiguration(
    @Autowired private val env: Environment,
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val tracingRequestInterceptor: TracingRequestInterceptor,
    @Autowired private val apiKeyRequestInterceptor: ApiKeyRequestInterceptor
) {
    @Bean
    fun statsApi(): StatsApi =
        StatsApiBuilder()
            .build(
                env = statsEnvironment(),
                mapper = mapper,
                interceptors = listOf(tracingRequestInterceptor, apiKeyRequestInterceptor)
            )

    fun statsEnvironment(): com.wutsi.stats.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            com.wutsi.stats.Environment.PRODUCTION
        else
            com.wutsi.stats.Environment.SANDBOX
}
