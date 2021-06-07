package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import com.wutsi.newsletter.service.FilterContext
import org.jsoup.nodes.Document

class ImageFilter : Filter {
    companion object {
        val IMAGE_STYLE = "max-width: 100%;" +
            "display: block;" +
            "height: auto;" +
            "margin: 0 auto;"
        val IMAGE_BORDER_STYLE = "border: 1px solid lightgray"
        val IMAGE_STRETCHED_STYLE = "width: 100%"

        val CAPTION_STYLE = "text-align: center;" +
            "text-decoration: underline;" +
            "width: 100%;"
    }

    override fun filter(doc: Document, context: FilterContext): Document {
        doc.select("figure img")
            .forEach {
                val style = StringBuilder(IMAGE_STYLE)
                if (it.hasClass("border")) {
                    style.append(IMAGE_BORDER_STYLE)
                }
                if (it.hasClass("stretched")) {
                    style.append(IMAGE_STRETCHED_STYLE)
                }
                it.attr("style", style.toString())
            }
        doc.select("figure figcaption")
            .forEach {
                it.attr("style", CAPTION_STYLE)
            }
        return doc
    }
}
