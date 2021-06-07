package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import com.wutsi.newsletter.service.FilterContext
import org.jsoup.nodes.Document

class ButtonFilter : Filter {
    companion object {
        val DIV_STYLE = "text-align: center;"

        val LINK_STYLE = "display: inline-block;" +
            " font-weight: 400;" +
            " color: #FFFFFF;" +
            " background-color: #1D7EDF;" +
            " text-align: center;" +
            " vertical-align: middle;" +
            "border: 1px solid transparent;" +
            " padding: .375rem .75rem;" +
            " line-height: 1.5;" +
            " text-decoration: none"
    }

    override fun filter(doc: Document, context: FilterContext): Document {
        doc.select("div.button")
            .forEach {
                it.attr("style", DIV_STYLE)
            }
        doc.select("div.button a")
            .forEach {
                it.attr("style", LINK_STYLE)
            }
        return doc
    }
}
