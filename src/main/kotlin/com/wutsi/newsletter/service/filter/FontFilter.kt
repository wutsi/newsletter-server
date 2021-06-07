package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import com.wutsi.newsletter.service.FilterContext
import org.jsoup.nodes.Document

class FontFilter : Filter {
    companion object {
        val STYLE = "font-family: 'PT Sans', sans-serif;"
    }

    override fun filter(doc: Document, context: FilterContext): Document {
        doc.select("blockquote,div,h1,h2,h3,h4,h5,h6,ol,ul,p")
            .forEach {
                val style = it.attr("style")
                it.attr("style", "$STYLE;$style")
            }
        return doc
    }
}
