package com.wutsi.newsletter.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.email.event.DeliverySubmittedEventPayload
import com.wutsi.email.event.EmailEventType
import com.wutsi.newsletter.delegate.ShareDelegate
import com.wutsi.platform.site.SiteProvider
import com.wutsi.site.SiteAttribute
import com.wutsi.site.dto.Attribute
import com.wutsi.site.dto.Site
import com.wutsi.story.StoryApi
import com.wutsi.story.dto.GetStoryResponse
import com.wutsi.story.dto.Story
import com.wutsi.stream.EventStream
import com.wutsi.user.UserApi
import com.wutsi.user.dto.Follower
import com.wutsi.user.dto.GetUserResponse
import com.wutsi.user.dto.SearchFollowerResponse
import com.wutsi.user.dto.User
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ShareControllerTest : ControllerTestBase() {
    @LocalServerPort
    private val port = 0

    @MockBean
    private lateinit var siteProvider: SiteProvider

    @MockBean
    private lateinit var storyApi: StoryApi

    @MockBean
    private lateinit var userApi: UserApi

    @MockBean
    private lateinit var eventStream: EventStream

    @BeforeTest
    override fun setUp() {
        super.setUp()

        val site = createSite()
        doReturn(site).whenever(siteProvider).get(any())
    }

    @Test
    fun `send to followers`() {
        login("newsletter")

        val story = createStory()
        doReturn(GetStoryResponse(story)).whenever(storyApi).get(any())

        val user = createUser(1)
        doReturn(GetUserResponse(user)).whenever(userApi).getUser(any())

        val follower = createUser(2, "js@gmail.com", "John Smith")
        val followers = listOf(Follower(userId = user.id, followerUserId = follower.id))
        doReturn(GetUserResponse(follower)).whenever(userApi).getUser(2L)
        doReturn(SearchFollowerResponse(followers)).whenever(userApi).searchFollowers(eq(1L), anyOrNull(), any(), any())

        val url = "http://127.0.0.1:$port/v1/newsletter/share?story-id=1"
        get(url, Any::class.java)

        val payload = argumentCaptor<DeliverySubmittedEventPayload>()
        verify(eventStream).publish(eq(EmailEventType.DELIVERY_SUBMITTED.urn), payload.capture())

        assertEquals("text/html", payload.firstValue.request.contentType)
        assertEquals(story.title, payload.firstValue.request.subject)
        assertEquals(ShareDelegate.CAMPAIGN, payload.firstValue.request.campaign)
        assertEquals(story.siteId, payload.firstValue.request.siteId)
        assertEquals(user.id, payload.firstValue.request.sender.userId)
        assertEquals(follower.fullName, payload.firstValue.request.recipient.displayName)
        assertEquals(follower.email, payload.firstValue.request.recipient.email)
    }

    @Test
    fun `donot send to followers when email is not enabled`() {
        login("newsletter")

        val site = createSite(attributes = emptyList())
        doReturn(site).whenever(siteProvider).get(any())

        val story = createStory()
        doReturn(GetStoryResponse(story)).whenever(storyApi).get(any())

        val url = "http://127.0.0.1:$port/v1/newsletter/share?story-id=1"
        get(url, Any::class.java)

        verify(eventStream, never()).publish(any(), any())
    }

    @Test
    fun `donot send to followers when user has no followers`() {
        login("newsletter")

        val story = createStory()
        doReturn(GetStoryResponse(story)).whenever(storyApi).get(any())

        val user = createUser(1)
        doReturn(GetUserResponse(user)).whenever(userApi).getUser(any())

        doReturn(SearchFollowerResponse(emptyList())).whenever(userApi).searchFollowers(eq(1L), anyOrNull(), any(), any())

        val url = "http://127.0.0.1:$port/v1/newsletter/share?story-id=1"
        get(url, Any::class.java)

        verify(eventStream, never()).publish(any(), any())
    }

    private fun createStory(userId: Long = 1) = Story(
        id = 123L,
        userId = userId,
        title = "This is a story title",
        slug = "/read/123/this-is-a-story-title",
        content = "{}"
    )

    private fun createUser(id: Long = 1, email: String? = "ray.sponsible@gmail.com", fullName: String = "Ray Sponsible") = User(
        id = id,
        email = email,
        fullName = fullName
    )

    private fun createSite(
        attributes: List<Attribute> = listOf(
            Attribute(SiteAttribute.NEWSLETTER_ENABLED.urn, "true")
        )
    ) = Site(
        id = 1,
        domainName = "www.wutsi.com",
        websiteUrl = "https://www.wutsi.com",
        attributes = attributes
    )
}
