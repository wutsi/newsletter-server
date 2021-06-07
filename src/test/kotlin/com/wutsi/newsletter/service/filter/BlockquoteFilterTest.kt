package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.FilterContext
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BlockquoteFilterTest {
    val context = FilterContext()
    val filter = BlockquoteFilter()
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<blockquote>" +
            "<p>Everything should be made as simple as possible, but no simpler</p>" +
            "<footer>A. Einstein</footer>" +
            "</blockquote>" +
            "</body></html>"
    )

    @Test
    fun filter() {
        val result = filter.filter(doc, context).html()
        assertEquals(
            "<html>\n" +
                " <head></head>\n" +
                " <body class=\"wutsi-mail-content\">\n" +
                "  <blockquote>\n" +
                "   <p style=\"${BlockquoteFilter.PARAGRAPH_STYLE}\">Everything should be made as simple as possible, but no simpler</p>\n" +
                "   <footer style=\"${BlockquoteFilter.FOOTER_STYLE}\">\n" +
                "    A. Einstein\n" +
                "   </footer>\n" +
                "  </blockquote>\n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
