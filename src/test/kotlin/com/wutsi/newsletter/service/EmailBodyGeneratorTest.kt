package com.wutsi.newsletter.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.site.dto.Site
import com.wutsi.story.dto.GetStoryResponse
import com.wutsi.story.dto.Story
import com.wutsi.user.dto.User
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.text.SimpleDateFormat

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal class EmailBodyGeneratorTest {
    @Autowired
    private lateinit var generator: EmailBodyGenerator

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun generate() {
        val story = objectMapper.readValue(EmailBodyGenerator::class.java.getResourceAsStream("/story.json"), GetStoryResponse::class.java).story
        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val result = generator.generate(story, site, user)

        println(result)
        val expected = IOUtils.toString(EmailBodyGenerator::class.java.getResourceAsStream("/story.html"), "utf-8")
        assertEquals(sanitizeHtml(expected), sanitizeHtml(result))
    }

    private fun sanitizeHtml(html: String): String =
        html.replace("\\s+".toRegex(), " ")
            .trimIndent()
            .trim()

    @Test
    fun scope() {
        val story = createStory(77, 5)
        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val scope = generator.scope(story, site, user, "Yo Man")

        assertEquals("https://int-com-wutsi-track.herokuapp.com/mail/pixel/77.png?u=7&d=5&c=newsletter", scope["pixelUrl"])
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
        publishedDateTime = SimpleDateFormat("yyyy-MM-dd").parse("2020-01-07")
    )
}
