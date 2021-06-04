package com.wutsi.newsletter.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.newsletter.util.Html.sanitizeHtml
import com.wutsi.site.dto.Site
import com.wutsi.stats.StatsApi
import com.wutsi.stats.dto.SearchViewResponse
import com.wutsi.stats.dto.View
import com.wutsi.story.dto.Story
import com.wutsi.user.dto.UserSummary
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.text.SimpleDateFormat
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal class DigestEmailBodyGeneratorTest {
    @Autowired
    private lateinit var generator: DigestEmailBodyGenerator

    @MockBean
    private lateinit var newsletter: NewsletterEmailBodyGenerator

    @MockBean
    private lateinit var statsApi: StatsApi

    private val user = createUser(1, "Ray Sponsible", "ray.sponsible@gmail.com")
    private val site = createSite()
    private val stories = listOf(
        createStory(1L),
        createStory(2L),
        createStory(3L)
    )

    @BeforeEach
    fun setUp() {
        doReturn(SearchViewResponse(emptyList())).whenever(statsApi)
            .views(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `do not generate email to viewed stories`() {
        val views = listOf(
            createView(1),
            createView(2),
            createView(3)
        )
        doReturn(SearchViewResponse(views)).whenever(statsApi)
            .views(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        val result = generator.generate(stories, site, user, LocalDate.now().minusDays(7), LocalDate.now())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `generate digest`() {
        doReturn("1")
            .doReturn("2")
            .doReturn("3").whenever(newsletter).generate(any(), any(), any())

        val result = generator.generate(stories, site, user, LocalDate.now().minusDays(7), LocalDate.now())

        println(result)
        val expected = IOUtils.toString(DigestEmailBodyGeneratorTest::class.java.getResourceAsStream("/digest.html"), "utf-8")
        Assertions.assertEquals(sanitizeHtml(expected), sanitizeHtml(result))
    }

    private fun createSite() = Site(
        id = 1L,
        name = "wutsi.com",
        displayName = "Wutsi",
        websiteUrl = "https://www.wutsi.com"
    )

    private fun createUserSummary(id: Long, fullName: String, email: String, name: String = "ray.sponsible") = UserSummary(
        id = id,
        name = name,
        fullName = fullName,
        email = email,
        language = "fr"
    )

    private fun createUser(id: Long, fullName: String, email: String, name: String = "ray.sponsible") = UserSummary(
        id = id,
        name = name,
        fullName = fullName,
        email = email,
        language = "fr"
    )

    private fun createStory(id: Long, title: String = "Sample Story", access: String = "PUBLIC") = Story(
        id = id,
        title = title,
        slug = "/read/$id/sample-story",
        userId = 11,
        siteId = 1,
        content = "This is the content",
        publishedDateTime = SimpleDateFormat("yyyy-MM-dd").parse("2020-01-07").time,
        access = access
    )

    private fun createView(storyId: Long) = View(
        storyId = storyId
    )
}
