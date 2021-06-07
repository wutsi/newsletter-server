package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import com.wutsi.newsletter.service.FilterContext
import org.jsoup.nodes.Document

class LinkToolFilter : Filter {
    companion object {
        val STYLE_LINK = "text-decoration: none;"

        val STYLE_CONTAINER = "border: 1px solid lightgray;" +
            "border-radius: 3px;" +
            "display: flex;" +
            "flex-wrap: wrap;" +
            "margin: 1em 0;" +
            "padding: 1em"

        val STYLE_META_CONTAINER = "width: 70%;" +
            "font-size: 80%"

        val STYLE_IMAGE_CONTAINER = "width: 30%;"

        val STYLE_IMAGE = "max-width: 100%;" +
            "margin: 0 auto;" +
            "border: 1px solid lightgray"
    }

    override fun filter(doc: Document, context: FilterContext): Document {
        doc.select("a.link-tool")
            .forEach {
                it.attr("style", STYLE_LINK)
            }
        doc.select("div.link-tool")
            .forEach {
                it.attr("style", STYLE_CONTAINER)
            }
        doc.select("div.link-tool .meta")
            .forEach {
                it.attr("style", STYLE_META_CONTAINER)
            }
        doc.select("div.link-tool .image")
            .forEach {
                it.attr("style", STYLE_IMAGE_CONTAINER)
            }
        doc.select("div.link-tool .image img")
            .forEach {
                it.attr("style", STYLE_IMAGE)
            }
        return doc
    }
}
