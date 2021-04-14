package com.wutsi.newsletter.service

import com.wutsi.newsletter.service.filter.BlockquoteFilter
import com.wutsi.newsletter.service.filter.ButtonFilter
import com.wutsi.newsletter.service.filter.FontFilter
import com.wutsi.newsletter.service.filter.HrFilter
import com.wutsi.newsletter.service.filter.ImageFilter
import com.wutsi.newsletter.service.filter.LinkToolFilter
import com.wutsi.newsletter.service.filter.PreFilter
import com.wutsi.newsletter.service.filter.YouTubeFilter
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class FilterSetTest {
    @Autowired
    private lateinit var filterSet: FilterSet

    @Test
    fun validate() {
        assertEquals(8, filterSet.filters.size)
        assertTrue(filterSet.filters[0] is BlockquoteFilter)
        assertTrue(filterSet.filters[1] is ImageFilter)
        assertTrue(filterSet.filters[2] is HrFilter)
        assertTrue(filterSet.filters[3] is LinkToolFilter)
        assertTrue(filterSet.filters[4] is PreFilter)
        assertTrue(filterSet.filters[5] is YouTubeFilter)
        assertTrue(filterSet.filters[6] is ButtonFilter)
        assertTrue(filterSet.filters[7] is FontFilter)
    }
}
