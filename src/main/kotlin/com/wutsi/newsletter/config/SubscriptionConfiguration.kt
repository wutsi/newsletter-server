package com.wutsi.newsletter.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.security.apikey.ApiKeyRequestInterceptor
import com.wutsi.subscription.SubscriptionApi
import com.wutsi.subscription.SubscriptionApiBuilder
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
public class SubscriptionConfiguration(
    @Autowired private val env: Environment,
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val tracingRequestInterceptor: TracingRequestInterceptor,
    @Autowired private val apiKeyRequestInterceptor: ApiKeyRequestInterceptor
) {
    @Bean
    fun subscriptionApi(): SubscriptionApi =
        SubscriptionApiBuilder()
            .build(
                env = subscriptionEnvironment(),
                mapper = mapper,
                interceptors = listOf(tracingRequestInterceptor, apiKeyRequestInterceptor)
            )

    fun subscriptionEnvironment(): com.wutsi.subscription.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            com.wutsi.subscription.Environment.PRODUCTION
        else
            com.wutsi.subscription.Environment.SANDBOX
}
