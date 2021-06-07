package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.FilterContext
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PreFilterTest {
    val context = FilterContext()
    val filter = PreFilter()
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<pre>" +
            "Yo Man" +
            "</pre>" +
            "</body></html>"
    )

    @Test
    fun filter() {
        val result = filter.filter(doc, context).html()
        assertEquals(
            "<html>\n" +
                " <head></head>\n" +
                " <body class=\"wutsi-mail-content\">\n" +
                "  <pre style=\"${PreFilter.STYLE}\">Yo Man</pre>\n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
