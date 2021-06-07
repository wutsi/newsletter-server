package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.FilterContext
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LinkToolFilterTest {
    val context = FilterContext()
    val filter = LinkToolFilter()
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<a class=\"link-tool\" href=\"https://wwww.foo.com\">" +
            " <div class=\"link-tool\">" +
            "  <div class=\"meta\">Description</div>" +
            "  <div class=\"image\">" +
            "   <img src=\"1.png\">" +
            "  </div>" +
            " </div>" +
            "</a>" +
            "</body></html>"
    )

    @Test
    fun filter() {
        val result = filter.filter(doc, context).html()
        assertEquals(
            "<html>\n" +
                " <head></head>\n" +
                " <body class=\"wutsi-mail-content\">\n" +
                "  <a class=\"link-tool\" href=\"https://wwww.foo.com\" style=\"text-decoration: none;\"> \n" +
                "   <div class=\"link-tool\" style=\"border: 1px solid lightgray;border-radius: 3px;display: flex;flex-wrap: wrap;margin: 1em 0;padding: 1em\"> \n" +
                "    <div class=\"meta\" style=\"width: 70%;font-size: 80%\">\n" +
                "     Description\n" +
                "    </div> \n" +
                "    <div class=\"image\" style=\"width: 30%;\"> \n" +
                "     <img src=\"1.png\" style=\"max-width: 100%;margin: 0 auto;border: 1px solid lightgray\"> \n" +
                "    </div> \n" +
                "   </div></a>\n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
