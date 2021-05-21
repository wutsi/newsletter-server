package com.wutsi.newsletter.config

import com.wutsi.stream.EventStream
import com.wutsi.stream.EventSubscription
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
public class WutsiBlogServiceConfiguration(
    private val eventStream: EventStream
) {
    @Bean
    fun wutsiBlogServiceSubscription() = EventSubscription("wutsi-blog-service", eventStream)
}
