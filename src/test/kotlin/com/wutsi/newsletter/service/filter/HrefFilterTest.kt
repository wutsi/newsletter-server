package com.wutsi.newsletter.service.filter

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.newsletter.service.FilterContext
import com.wutsi.newsletter.service.TrackingContext
import com.wutsi.site.dto.Site
import com.wutsi.user.dto.UserSummary
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

internal class HrefFilterTest {
    val context = FilterContext(
        user = UserSummary(id = 777),
        site = Site(websiteUrl = "http://www.wutsi.com"),
        campaign = "XxX"
    )
    val trackingContext = mock<TrackingContext>()
    val filter = HrefFilter(trackingContext)
    val doc = Jsoup.parse(
        "<html><body class=\"wutsi-mail-content\">" +
            "<a href=\"https://wwww.google.com\">Google</a>\n" +
            "<h1>Section1</h1>\n" +
            "<a name=\"foo\" />\n" +
            "</body></html>"
    )

    @Test
    fun filter() {
        doReturn("111").whenever(trackingContext).deviceId(any())
        doReturn("222").whenever(trackingContext).hitId(any())

        val result = filter.filter(doc, context).html()
        kotlin.test.assertEquals(
            "<html>\n" +
                " <head></head>\n" +
                " <body class=\"wutsi-mail-content\">\n" +
                "  <a href=\"http://www.wutsi.com/mail/track/link?u=777&amp;c=XxX&amp;hid=222&amp;did=111&amp;url=https://wwww.google.com\">Google</a> \n" +
                "  <h1>Section1</h1> \n" +
                "  <a name=\"foo\"></a> \n" +
                " </body>\n" +
                "</html>",
            result
        )
    }
}
