package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.FilterContext
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ButtonFilterTest {
    val context = FilterContext()
    val filter = ButtonFilter()
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<div class='button'>" +
            "<a href='https://www.google.ca'>Yo</a>" +
            "</div>" +
            "</body></html>"
    )

    @Test
    fun filter() {
        val result = filter.filter(doc, context).html()
        assertEquals(
            "<html>\n" +
                " <head></head>\n" +
                " <body class=\"wutsi-mail-content\">\n" +
                "  <div class=\"button\" style=\"${ButtonFilter.DIV_STYLE}\">\n" +
                "   <a href=\"https://www.google.ca\" style=\"${ButtonFilter.LINK_STYLE}\">Yo</a>\n" +
                "  </div>\n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
