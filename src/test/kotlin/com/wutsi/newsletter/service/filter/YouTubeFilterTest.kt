package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.FilterContext
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class YouTubeFilterTest {
    val context = FilterContext()
    val filter = YouTubeFilter()
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<div class=\"youtube\" data-id=\"123\"></div>" +
            "</body></html>"
    )

    @Test
    fun filter() {
        val result = filter.filter(doc, context).html()
        assertEquals(
            "<html>\n" +
                " <head></head>\n" +
                " <body class=\"wutsi-mail-content\">\n" +
                "  <div class=\"youtube\" data-id=\"123\" style=\"${YouTubeFilter.STYLE_CONTAINER}\">\n" +
                "   <a href=\"https://www.youtube.com/watch?v=123\"><img style=\"${YouTubeFilter.STYLE_IMAGE}\" src=\"https://img.youtube.com/vi/123/0.jpg\">\n" +
                "    <div style=\"${YouTubeFilter.STYLE_CONTROLS}\">\n" +
                "     <img width=\"32\" height=\"32\" src=\"https://prod-wutsi.s3.amazonaws.com/static/wutsi-blog-web/assets/wutsi/img/mail/play-video.png\">\n" +
                "    </div></a>\n" +
                "  </div>\n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
