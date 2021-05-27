package com.wutsi.newsletter.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.site.dto.Site
import com.wutsi.story.dto.GetStoryResponse
import com.wutsi.story.dto.Story
import com.wutsi.subscription.SubscriptionApi
import com.wutsi.subscription.dto.SearchSubscriptionResponse
import com.wutsi.user.UserApi
import com.wutsi.user.dto.GetUserResponse
import com.wutsi.user.dto.SearchFollowerResponse
import com.wutsi.user.dto.User
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.text.SimpleDateFormat
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal class EmailBodyGeneratorTest {
    @Autowired
    private lateinit var generator: EmailBodyGenerator

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var subscriptionApi: SubscriptionApi

    @MockBean
    private lateinit var userApi: UserApi

    @BeforeEach
    fun setUp() {
        doReturn(SearchFollowerResponse()).whenever(userApi).searchFollowers(any(), any(), any(), any())
        doReturn(SearchSubscriptionResponse()).whenever(subscriptionApi).partnerSubscriptions(any(), any(), any())
    }

    @Test
    fun `generate email for Story with PUBLIC access`() {
        val story = objectMapper.readValue(EmailBodyGenerator::class.java.getResourceAsStream("/story.json"), GetStoryResponse::class.java).story
        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val result = generator.generate(story, site, user)

        println(result)
        val expected = IOUtils.toString(EmailBodyGenerator::class.java.getResourceAsStream("/story.html"), "utf-8")
        assertEquals(sanitizeHtml(expected), sanitizeHtml(result))
    }

    @Test
    fun `generate email for Story with SUBSCRIBER access`() {
        val story =
            objectMapper.readValue(EmailBodyGenerator::class.java.getResourceAsStream("/story-subscriber.json"), GetStoryResponse::class.java).story
        val blog = createUser(story.userId, "Roger Milla", "roger.milla@gmail.com", name = "roger.milla")
        doReturn(GetUserResponse(blog)).whenever(userApi).getUser(story.userId)

        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val result = generator.generate(story, site, user)

        println(result)
        val expected = IOUtils.toString(EmailBodyGenerator::class.java.getResourceAsStream("/story-subscriber.html"), "utf-8")
        assertEquals(sanitizeHtml(expected), sanitizeHtml(result))
    }

    @Test
    fun `generate email for Story with PREMIUM_SUBSCRIBER access`() {
        val story =
            objectMapper.readValue(EmailBodyGenerator::class.java.getResourceAsStream("/story-premium.json"), GetStoryResponse::class.java).story
        val blog = createUser(story.userId, "Roger Milla", "roger.milla@gmail.com", name = "roger.milla")
        doReturn(GetUserResponse(blog)).whenever(userApi).getUser(story.userId)

        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val result = generator.generate(story, site, user)

        println(result)
        val expected = IOUtils.toString(EmailBodyGenerator::class.java.getResourceAsStream("/story-premium.html"), "utf-8")
        assertEquals(sanitizeHtml(expected), sanitizeHtml(result))
    }

    private fun sanitizeHtml(html: String): String =
        html.replace("\\s+".toRegex(), " ")
            .replace(">\\s*".toRegex(), ">")
            .replace("\\s*<".toRegex(), "<")
            .trimIndent()
            .trim()

    @Test
    fun `scope for story with PUBLIC access`() {
        val story = createStory(77, 5)
        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val scope = generator.scope(story, site, user, "Yo Man", true)

        assertEquals(11, scope.size)
        assertEquals("https://www.wutsi.com/story/pixel/77.png?u=7&d=5&c=newsletter", scope["pixelUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story", scope["storyUrl"])
        assertEquals("Sample Story", scope["title"])
        assertEquals("7 janv. 2020", scope["publishedDate"])
        assertEquals("Yo Man", scope["content"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?like=1", scope["likeUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?comment=1", scope["commentUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?share=1", scope["shareUrl"])
        assertEquals(
            "Si tu as aim&eacute; cette Storie de <a href=\"https://www.wutsi.com/@/ray.sponsible\">Ray Sponsible</a>, Pourquoi ne pas la partager?",
            scope["shareText"]
        )
        assertEquals("Partage la Storie", scope["shareButton"])
        assertNull(scope["subscribe"])
    }

    @Test
    fun `scope for story with SUBSCRIBER access`() {
        val story = createStory(77, 5, access = "SUBSCRIBER")
        val blog = createUser(story.userId, "Roger Milla", "roger.milla@gmail.com", name = "roger.milla")
        doReturn(GetUserResponse(blog)).whenever(userApi).getUser(story.userId)

        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val scope = generator.scope(story, site, user, "Yo Man", false)

        assertEquals(11, scope.size)
        assertEquals("https://www.wutsi.com/story/pixel/77.png?u=7&d=5&c=newsletter", scope["pixelUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story", scope["storyUrl"])
        assertEquals("Sample Story", scope["title"])
        assertEquals("7 janv. 2020", scope["publishedDate"])
        assertEquals("Yo Man", scope["content"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?like=1", scope["likeUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?comment=1", scope["commentUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?share=1", scope["shareUrl"])
        assertEquals(
            "Si tu as aim&eacute; cette Storie de <a href=\"https://www.wutsi.com/@/ray.sponsible\">Ray Sponsible</a>, Pourquoi ne pas la partager?",
            scope["shareText"]
        )
        assertEquals("Partage la Storie", scope["shareButton"])

        val subscribe = scope["subscribe"] as Map<String, String>
        assertNotNull(subscribe)
        assertEquals("Cette Storie est pour les <b>Abonn&eacute;s</b>", subscribe["title"])
        assertEquals("https://www.wutsi.com/@/roger.milla/subscribe", subscribe["subscribeUrl"])
        assertEquals("Abonne-toi pour Lire", subscribe["subscribeButton"])
    }

    @Test
    fun `scope for story with PREMIUM_SUBSCRIBER access`() {
        val story = createStory(77, 5, access = "PREMIUM_SUBSCRIBER")
        val blog = createUser(story.userId, "Roger Milla", "roger.milla@gmail.com", name = "roger.milla")
        doReturn(GetUserResponse(blog)).whenever(userApi).getUser(story.userId)

        val user = createUser(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val scope = generator.scope(story, site, user, "Yo Man", false)

        assertEquals(11, scope.size)
        assertEquals("https://www.wutsi.com/story/pixel/77.png?u=7&d=5&c=newsletter", scope["pixelUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story", scope["storyUrl"])
        assertEquals("Sample Story", scope["title"])
        assertEquals("7 janv. 2020", scope["publishedDate"])
        assertEquals("Yo Man", scope["content"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?like=1", scope["likeUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?comment=1", scope["commentUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story?share=1", scope["shareUrl"])
        assertEquals(
            "Si tu as aim&eacute; cette Storie de <a href=\"https://www.wutsi.com/@/ray.sponsible\">Ray Sponsible</a>, Pourquoi ne pas la partager?",
            scope["shareText"]
        )
        assertEquals("Partage la Storie", scope["shareButton"])

        val subscribe = scope["subscribe"] as Map<String, String>
        assertNotNull(subscribe)
        assertEquals("Cette Storie est pour les <b>Abonn&eacute;s Premium</b>", subscribe["title"])
        assertEquals("https://www.wutsi.com/@/roger.milla/subscribe?premium=1", subscribe["subscribeUrl"])
        assertEquals("Abonne-toi pour Lire", subscribe["subscribeButton"])
    }

    private fun createSite() = Site(
        id = 1L,
        name = "wutsi.com",
        displayName = "Wutsi",
        websiteUrl = "https://www.wutsi.com"
    )

    private fun createUser(id: Long, fullName: String, email: String, name: String = "ray.sponsible") = User(
        id = id,
        name = name,
        fullName = fullName,
        email = email,
        language = "fr"
    )

    private fun createStory(id: Long, durationMinutes: Int, title: String = "Sample Story", access: String = "PUBLIC") = Story(
        id = id,
        title = title,
        slug = "/read/$id/sample-story",
        readingMinutes = durationMinutes,
        userId = 11,
        siteId = 1,
        content = "This is the content",
        publishedDateTime = SimpleDateFormat("yyyy-MM-dd").parse("2020-01-07").time,
        access = access
    )
}
