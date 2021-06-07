package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import com.wutsi.newsletter.service.FilterContext
import org.jsoup.nodes.Document

class BlockquoteFilter : Filter {
    companion object {
        val PARAGRAPH_STYLE = "font-size: 1.4em;" +
            "font-weight: bold;" +
            "margin: 0 auto;" +
            "text-align: center;" +
            "width: 50%;"
        val FOOTER_STYLE = "text-align: center;" +
            "text-decoration: underline;" +
            "width: 100%;"
    }

    override fun filter(doc: Document, context: FilterContext): Document {
        doc.select("blockquote p")
            .forEach {
                it.attr("style", PARAGRAPH_STYLE)
            }
        doc.select("blockquote footer")
            .forEach {
                it.attr("style", FOOTER_STYLE)
            }
        return doc
    }
}
