package com.wutsi.newsletter.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.editorjs.dom.EJSDocument
import com.wutsi.site.dto.Site
import com.wutsi.story.dto.Story
import com.wutsi.user.dto.User
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.support.ResourceBundleMessageSource
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal class EmailBodyGeneratorTest {
    private lateinit var editorJSService: EditorJSService
    private lateinit var filters: FilterSet
    private val messageSource: ResourceBundleMessageSource = ResourceBundleMessageSource()
    private val trackingUrl: String = "https://track.wutsi.com"

    private lateinit var generator: EmailBodyGenerator

    @BeforeEach
    fun setUp() {
        messageSource.setBasename("messages")

        editorJSService = mock()
        filters = mock()
        generator = EmailBodyGenerator(editorJSService, filters, messageSource, trackingUrl)
    }

    @Test
    fun generate() {
        val doc = EJSDocument()
        doReturn(doc).whenever(editorJSService).fromJson(any())

        val html = "Bonjour &Eacute;ccl&eacute;siaste!"
        doReturn(html).whenever(editorJSService).toHtml(doc)
        doReturn(Jsoup.parse(html)).whenever(filters).filter(any())

        val story = createStory(77, 5)
        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val result = generator.generate(story, site, user)

        val expected = IOUtils.toString(EmailBodyGenerator::class.java.getResourceAsStream("/body.html"), "utf-8")
        assertEquals(expected.trimIndent(), result.trimIndent())
    }

    @Test
    fun scope() {
        val story = createStory(77, 5)
        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val scope = generator.scope(story, site, user, "Yo Man")

        assertEquals("https://track.wutsi.com/mail/pixel/77.png?u=7&d=5&c=newsletter", scope["pixelUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story", scope["storyUrl"])
        assertEquals("Sample Story", scope["title"])
        assertEquals("7 janv. 2020", scope["publishedDate"])
        assertEquals("Yo Man", scope["content"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?like=1", scope["likeUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?comment=1", scope["commentUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?share=1", scope["shareUrl"])
        assertEquals(
            "Si tu as aim&eacute; cette Storie de <a href=\"https://www.wutsi.com/@/\">Ray Sponsible</a>, Pourquoi ne pas la partager?",
            scope["shareText"]
        )
        assertEquals("Partage la Storie", scope["shareButton"])
    }

    private fun createSite() = Site(
        id = 1L,
        name = "wutsi.com",
        displayName = "Wutsi",
        websiteUrl = "https://www.wutsi.com"
    )

    private fun createUser(id: Long, fullName: String, email: String) = User(
        id = id,
        fullName = fullName,
        email = email,
        language = "fr"
    )

    private fun createStory(id: Long, durationMinutes: Int, title: String = "Sample Story") = Story(
        id = id,
        title = title,
        slug = "/read/$id/sample-story",
        readingMinutes = durationMinutes,
        userId = 11,
        siteId = 1,
        content = "This is the content",
        publishedDateTime = OffsetDateTime.of(2020, 1, 7, 10, 30, 0, 0, ZoneOffset.UTC)
    )
}
