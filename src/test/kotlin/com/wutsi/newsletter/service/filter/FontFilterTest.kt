package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.FilterContext
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FontFilterTest {
    val context = FilterContext()
    val filter = FontFilter()
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<blockquote style=\"font-size: 1.4em\">" +
            "<p>Everything should be made as simple as possible, but no simpler</p>" +
            "<footer>A. Einstein</footer>" +
            "</blockquote>" +
            "<figure><img src=\"1.ong\">" +
            "<p>Hellow world</p>" +
            "<hr>" +
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
                "  <blockquote style=\"font-family: 'PT Sans', sans-serif;;font-size: 1.4em\">\n" +
                "   <p style=\"font-family: 'PT Sans', sans-serif;;\">Everything should be made as simple as possible, but no simpler</p>\n" +
                "   <footer>\n" +
                "    A. Einstein\n" +
                "   </footer>\n" +
                "  </blockquote>\n" +
                "  <figure>\n" +
                "   <img src=\"1.ong\">\n" +
                "   <p style=\"font-family: 'PT Sans', sans-serif;;\">Hellow world</p>\n" +
                "   <hr><a class=\"link-tool\" href=\"https://wwww.foo.com\"> \n" +
                "    <div class=\"link-tool\" style=\"font-family: 'PT Sans', sans-serif;;\"> \n" +
                "     <div class=\"meta\" style=\"font-family: 'PT Sans', sans-serif;;\">\n" +
                "      Description\n" +
                "     </div> \n" +
                "     <div class=\"image\" style=\"font-family: 'PT Sans', sans-serif;;\"> \n" +
                "      <img src=\"1.png\"> \n" +
                "     </div> \n" +
                "    </div></a>\n" +
                "  </figure>\n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
