package com.wutsi.newsletter.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.security.apikey.ApiKeyRequestInterceptor
import com.wutsi.site.SiteApi
import com.wutsi.site.SiteApiBuilder
import com.wutsi.tracing.TracingRequestInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
public class SiteConfiguration(
    @Autowired private val env: Environment,
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val tracingRequestInterceptor: TracingRequestInterceptor,
    @Autowired private val apiKeyRequestInterceptor: ApiKeyRequestInterceptor
) {
    @Bean
    fun siteApi(): SiteApi =
        SiteApiBuilder()
            .build(
                env = siteEnvironment(),
                mapper = mapper,
                interceptors = listOf(tracingRequestInterceptor, apiKeyRequestInterceptor)
            )

    fun siteEnvironment(): com.wutsi.site.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            com.wutsi.site.Environment.PRODUCTION
        else
            com.wutsi.site.Environment.SANDBOX
}
