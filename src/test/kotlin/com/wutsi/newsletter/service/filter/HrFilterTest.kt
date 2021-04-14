package com.wutsi.newsletter.service.filter

import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HrFilterTest {
    val filter = HrFilter()
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<hr>" +
            "</body></html>"
    )

    @Test
    fun filter() {
        val result = filter.filter(doc).html()
        assertEquals(
            "<html>\n" +
                " <head></head>\n" +
                " <body class=\"wutsi-mail-content\">\n" +
                "  <hr style=\"${HrFilter.STYLE}\">\n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
