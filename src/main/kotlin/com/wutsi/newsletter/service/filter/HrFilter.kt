package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import org.jsoup.nodes.Document

class HrFilter : Filter {
    companion object {
        val STYLE = "margin: 2em auto;" +
            "width: 50%"
    }

    override fun filter(doc: Document): Document {
        doc.select("hr")
            .forEach {
                it.attr("style", STYLE)
            }
        return doc
    }
}
