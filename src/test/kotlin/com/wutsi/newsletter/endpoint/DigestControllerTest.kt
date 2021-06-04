package com.wutsi.newsletter.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.email.event.DeliverySubmittedEventPayload
import com.wutsi.email.event.EmailEventType.DELIVERY_SUBMITTED
import com.wutsi.platform.site.SiteProvider
import com.wutsi.site.SiteAttribute.NEWSLETTER_ENABLED
import com.wutsi.site.dto.Attribute
import com.wutsi.site.dto.Site
import com.wutsi.stats.StatsApi
import com.wutsi.stats.dto.SearchViewResponse
import com.wutsi.story.StoryApi
import com.wutsi.story.dto.SearchStoryResponse
import com.wutsi.story.dto.Story
import com.wutsi.stream.EventStream
import com.wutsi.user.UserApi
import com.wutsi.user.dto.SearchUserResponse
import com.wutsi.user.dto.UserSummary
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DigestControllerTest : ControllerTestBase() {
    @LocalServerPort
    private val port = 0

    @MockBean
    private lateinit var userApi: UserApi

    @MockBean
    private lateinit var storyApi: StoryApi

    @MockBean
    private lateinit var statsApi: StatsApi

    @MockBean
    private lateinit var siteProvider: SiteProvider

    @MockBean
    private lateinit var eventStream: EventStream

    @Test
    operator fun invoke() {
        login("newsletter")

        val site = createSite()
        doReturn(site).whenever(siteProvider).get(any())

        doReturn(SearchViewResponse()).whenever(statsApi)
            .views(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        val stories = listOf(createStory(), createStory())
        doReturn(SearchStoryResponse(stories)).whenever(storyApi).published(any(), any(), any(), any())

        val users = listOf(createUser(1), createUser(2))
        doReturn(SearchUserResponse(users))
            .doReturn(SearchUserResponse())
            .whenever(userApi).searchUsers(anyOrNull(), any(), any())

        val url = "http://127.0.0.1:$port/v1/newsletter/digest?start-date=2020-01-01&end-date=2020-01-07"
        get(url, Any::class.java)

        val payload = argumentCaptor<DeliverySubmittedEventPayload>()
        verify(eventStream, times(2)).publish(eq(DELIVERY_SUBMITTED.urn), payload.capture())
    }

    private fun createStory(userId: Long = 1) = Story(
        id = 123L,
        userId = userId,
        title = "This is a story title",
        slug = "/read/123/this-is-a-story-title",
        content = "{}"
    )

    private fun createUser(id: Long = 1, fullName: String = "Ray Sponsible") = UserSummary(
        id = id,
        email = "ray$id.sponsible@gmail.com",
        fullName = fullName
    )

    private fun createSite(
        attributes: List<Attribute> = listOf(
            Attribute(NEWSLETTER_ENABLED.urn, "true")
        )
    ) = Site(
        id = 1,
        domainName = "www.wutsi.com",
        websiteUrl = "https://www.wutsi.com",
        attributes = attributes
    )
}
