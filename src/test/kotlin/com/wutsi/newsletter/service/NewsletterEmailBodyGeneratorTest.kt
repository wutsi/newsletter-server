package com.wutsi.newsletter.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.newsletter.delegate.ShareDelegate
import com.wutsi.newsletter.util.Html.sanitizeHtml
import com.wutsi.site.dto.Site
import com.wutsi.story.dto.GetStoryResponse
import com.wutsi.story.dto.Story
import com.wutsi.subscription.SubscriptionApi
import com.wutsi.subscription.dto.SearchSubscriptionResponse
import com.wutsi.user.UserApi
import com.wutsi.user.dto.GetUserResponse
import com.wutsi.user.dto.SearchFollowerResponse
import com.wutsi.user.dto.User
import com.wutsi.user.dto.UserSummary
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.text.SimpleDateFormat
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal class NewsletterEmailBodyGeneratorTest {
    companion object {
        const val HIT_ID = "1111"
        const val DEVICE_ID = "2222"
    }

    @Autowired
    private lateinit var generator: NewsletterEmailBodyGenerator

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var subscriptionApi: SubscriptionApi

    @MockBean
    private lateinit var userApi: UserApi

    @MockBean
    private lateinit var trackingContext: TrackingContext

    @BeforeEach
    fun setUp() {
        doReturn(SearchFollowerResponse()).whenever(userApi).searchFollowers(any(), any(), any(), any())
        doReturn(SearchSubscriptionResponse()).whenever(subscriptionApi).partnerSubscriptions(any(), any(), any())

        doReturn(HIT_ID).whenever(trackingContext).hitId(any())
        doReturn(DEVICE_ID).whenever(trackingContext).deviceId(any())
    }

    @Test
    fun `generate email for Story with PUBLIC access`() {
        val story = objectMapper.readValue(NewsletterEmailBodyGenerator::class.java.getResourceAsStream("/story.json"), GetStoryResponse::class.java).story
        val user = createUserSummary(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val result = generator.generate(story, site, user, ShareDelegate.CAMPAIGN)

        println(result)
        val expected = IOUtils.toString(NewsletterEmailBodyGenerator::class.java.getResourceAsStream("/story.html"), "utf-8")
        assertEquals(sanitizeHtml(expected), sanitizeHtml(result))
    }

    @Test
    fun `generate email for Story with SUBSCRIBER access`() {
        val story =
            objectMapper.readValue(
                NewsletterEmailBodyGenerator::class.java.getResourceAsStream("/story-subscriber.json"),
                GetStoryResponse::class.java
            ).story
        val blog = createUser(story.userId, "Roger Milla", "roger.milla@gmail.com", name = "roger.milla")
        doReturn(GetUserResponse(blog)).whenever(userApi).getUser(story.userId)

        val user = createUserSummary(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val result = generator.generate(story, site, user, ShareDelegate.CAMPAIGN)

        println(result)
        val expected = IOUtils.toString(NewsletterEmailBodyGenerator::class.java.getResourceAsStream("/story-subscriber.html"), "utf-8")
        assertEquals(sanitizeHtml(expected), sanitizeHtml(result))
    }

    @Test
    fun `generate email for Story with PREMIUM_SUBSCRIBER access`() {
        val story =
            objectMapper.readValue(NewsletterEmailBodyGenerator::class.java.getResourceAsStream("/story-premium.json"), GetStoryResponse::class.java).story
        val blog = createUser(story.userId, "Roger Milla", "roger.milla@gmail.com", name = "roger.milla")
        doReturn(GetUserResponse(blog)).whenever(userApi).getUser(story.userId)

        val user = createUserSummary(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val result = generator.generate(story, site, user, ShareDelegate.CAMPAIGN)

        println(result)
        val expected = IOUtils.toString(NewsletterEmailBodyGenerator::class.java.getResourceAsStream("/story-premium.html"), "utf-8")
        assertEquals(sanitizeHtml(expected), sanitizeHtml(result))
    }

    @Test
    fun `scope for story with PUBLIC access`() {
        val story = createStory(77, 5)
        val user = createUserSummary(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val scope = generator.scope(story, site, user, "Yo Man", true, ShareDelegate.CAMPAIGN)

        assertEquals(9, scope.size)
        assertEquals("https://www.wutsi.com/mail/track/77.png?u=7&d=5&c=newsletter&hid=$HIT_ID&did=$DEVICE_ID", scope["pixelUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story", scope["storyUrl"])
        assertEquals("Sample Story", scope["title"])
        assertEquals("7 janv. 2020", scope["publishedDate"])
        assertEquals("Yo Man", scope["content"])
        assertEquals("https://www.facebook.com/sharer/sharer.php?display=page&u=https://www.wutsi.com/read/77/sample-story", scope["shareFacebookUrl"])
        assertEquals("http://www.twitter.com/intent/tweet?url=https://www.wutsi.com/read/77/sample-story", scope["shareTwitterUrl"])
        assertEquals("https://www.linkedin.com/shareArticle?mini=true&url=https://www.wutsi.com/read/77/sample-story", scope["shareLinkedinUrl"])
        assertNull(scope["subscribe"])
    }

    @Test
    fun `scope for story with SUBSCRIBER access`() {
        val story = createStory(77, 5, access = "SUBSCRIBER")
        val blog = createUser(story.userId, "Roger Milla", "roger.milla@gmail.com", name = "roger.milla")
        doReturn(GetUserResponse(blog)).whenever(userApi).getUser(story.userId)

        val user = createUserSummary(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val scope = generator.scope(story, site, user, "Yo Man", false, ShareDelegate.CAMPAIGN)

        assertEquals(9, scope.size)
        assertEquals("https://www.wutsi.com/mail/track/77.png?u=7&d=5&c=newsletter&hid=$HIT_ID&did=$DEVICE_ID", scope["pixelUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story", scope["storyUrl"])
        assertEquals("Sample Story", scope["title"])
        assertEquals("7 janv. 2020", scope["publishedDate"])
        assertEquals("Yo Man", scope["content"])

        assertEquals("https://www.facebook.com/sharer/sharer.php?display=page&u=https://www.wutsi.com/read/77/sample-story", scope["shareFacebookUrl"])
        assertEquals("http://www.twitter.com/intent/tweet?url=https://www.wutsi.com/read/77/sample-story", scope["shareTwitterUrl"])
        assertEquals("https://www.linkedin.com/shareArticle?mini=true&url=https://www.wutsi.com/read/77/sample-story", scope["shareLinkedinUrl"])

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

        val user = createUserSummary(7, "Ray Sponsible", "ray.sponsible@gmail.com")
        val site = createSite()
        val scope = generator.scope(story, site, user, "Yo Man", false, ShareDelegate.CAMPAIGN)

        assertEquals(9, scope.size)
        assertEquals("https://www.wutsi.com/mail/track/77.png?u=7&d=5&c=newsletter&hid=$HIT_ID&did=$DEVICE_ID", scope["pixelUrl"])
        assertEquals("https://www.wutsi.com/read/77/sample-story", scope["storyUrl"])
        assertEquals("Sample Story", scope["title"])
        assertEquals("7 janv. 2020", scope["publishedDate"])
        assertEquals("Yo Man", scope["content"])

        assertEquals("https://www.facebook.com/sharer/sharer.php?display=page&u=https://www.wutsi.com/read/77/sample-story", scope["shareFacebookUrl"])
        assertEquals("http://www.twitter.com/intent/tweet?url=https://www.wutsi.com/read/77/sample-story", scope["shareTwitterUrl"])
        assertEquals("https://www.linkedin.com/shareArticle?mini=true&url=https://www.wutsi.com/read/77/sample-story", scope["shareLinkedinUrl"])

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

    private fun createUserSummary(id: Long, fullName: String, email: String, name: String = "ray.sponsible") = UserSummary(
        id = id,
        name = name,
        fullName = fullName,
        email = email,
        language = "fr"
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
