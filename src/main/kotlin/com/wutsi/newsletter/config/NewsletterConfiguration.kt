package com.wutsi.newsletter.config

import com.wutsi.newsletter.service.FilterSet
import com.wutsi.newsletter.service.TrackingContext
import com.wutsi.newsletter.service.filter.BlockquoteFilter
import com.wutsi.newsletter.service.filter.ButtonFilter
import com.wutsi.newsletter.service.filter.FontFilter
import com.wutsi.newsletter.service.filter.HrFilter
import com.wutsi.newsletter.service.filter.HrefFilter
import com.wutsi.newsletter.service.filter.ImageFilter
import com.wutsi.newsletter.service.filter.LinkToolFilter
import com.wutsi.newsletter.service.filter.PreFilter
import com.wutsi.newsletter.service.filter.YouTubeFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NewsletterConfiguration(private val trackingContext: TrackingContext) {
    @Bean
    fun newsletterFilterSet() = FilterSet(
        filters = listOf(
            BlockquoteFilter(),
            ImageFilter(),
            HrFilter(),
            LinkToolFilter(),
            PreFilter(),
            YouTubeFilter(),
            ButtonFilter(),

            // Before the last
            HrefFilter(trackingContext),

            // MUST BE THE LAST
            FontFilter()
        )
    )
}
