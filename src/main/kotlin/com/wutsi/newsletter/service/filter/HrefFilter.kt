package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import com.wutsi.newsletter.service.FilterContext
import com.wutsi.newsletter.service.TrackingContext
import org.jsoup.nodes.Document

class HrefFilter(private val trackingContext: TrackingContext) : Filter {
    override fun filter(doc: Document, context: FilterContext): Document {
        doc.select("a")
            .forEach {
                val href = it.attr("href")
                if (!href.isNullOrEmpty()) {
                    val url = "${context.site.websiteUrl}/mail/track/link" +
                        "?u=${context.user.id}" +
                        "&c=${context.campaign}" +
                        "&hid=${trackingContext.hitId(context.user)}" +
                        "&did=${trackingContext.deviceId(context.user)}" +
                        "&url=$href"
                    it.attr("href", url)
                }
            }
        return doc
    }
}
