package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.Filter
import com.wutsi.newsletter.service.FilterContext
import org.jsoup.nodes.Document

class YouTubeFilter : Filter {
    companion object {
        val STYLE_CONTAINER = "margin: 1em 0;"

        val STYLE_IMAGE = "display: block;" +
            "width: 100%;" +
            "margin: 0 auto;" +
            "border: 1px solid gray;"

        val STYLE_CONTROLS = "border: 1px solid gray;" +
            "padding: 2px"
    }

    override fun filter(doc: Document, context: FilterContext): Document {
        doc.select(".youtube")
            .forEach {
                it.attr("style", STYLE_CONTAINER)

                val id = it.attr("data-id")
                val innerHtml = "<a href=\"https://www.youtube.com/watch?v=$id\">" +
                    "<img style=\"$STYLE_IMAGE\" src=\"https://img.youtube.com/vi/$id/0.jpg\">" +
                    "<div style=\"$STYLE_CONTROLS\">" +
                    "<img width=\"32\" height=\"32\" src=\"https://prod-wutsi.s3.amazonaws.com/static/wutsi-blog-web/assets/wutsi/img/mail/play-video.png\">" +
                    "</div>" +
                    "</a>"
                it.append(innerHtml)
            }
        return doc
    }
}
