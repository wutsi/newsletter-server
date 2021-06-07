package com.wutsi.newsletter.service.filter

import com.wutsi.newsletter.service.FilterContext
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ImageFilterTest {
    val context = FilterContext()
    val filter = ImageFilter()
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<figure>" +
            "<img src=\"1.png\">" +
            "<figcaption>A. Einstein</figurecaption>" +
            "</figure>" +
            "</body></html>"
    )

    @Test
    fun filter() {
        val result = filter.filter(doc, context).html()
        assertEquals(
            "<html>\n" +
                " <head></head>\n" +
                " <body class=\"wutsi-mail-content\">\n" +
                "  <figure>\n" +
                "   <img src=\"1.png\" style=\"${ImageFilter.IMAGE_STYLE}\">\n" +
                "   <figcaption style=\"${ImageFilter.CAPTION_STYLE}\">\n" +
                "    A. Einstein\n" +
                "   </figcaption>\n" +
                "  </figure>\n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
