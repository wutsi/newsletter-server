package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import com.wutsi.newsletter.service.FilterContext
import org.jsoup.nodes.Document

class PreFilter : Filter {
    companion object {
        val STYLE = "background: #f8f8f8f8;" +
            "border: 1px solid lightgray;" +
            "border-radius: 5px;" +
            "padding: 1em;"
    }

    override fun filter(doc: Document, context: FilterContext): Document {
        doc.select("pre")
            .forEach {
                it.attr("style", STYLE)
            }
        return doc
    }
}
