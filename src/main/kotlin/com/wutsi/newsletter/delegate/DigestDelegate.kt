package com.wutsi.newsletter.delegate

import com.wutsi.email.dto.Address
import com.wutsi.email.dto.SendEmailRequest
import com.wutsi.email.dto.Sender
import com.wutsi.email.event.DeliverySubmittedEventPayload
import com.wutsi.email.event.EmailEventType
import com.wutsi.newsletter.service.DigestEmailBodyGenerator
import com.wutsi.platform.site.SiteProvider
import com.wutsi.site.SiteAttribute
import com.wutsi.site.dto.Site
import com.wutsi.story.StoryApi
import com.wutsi.story.dto.Story
import com.wutsi.stream.EventStream
import com.wutsi.user.UserApi
import com.wutsi.user.dto.User
import com.wutsi.user.dto.UserSummary
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Locale

@Service
public class DigestDelegate(
    private val storyApi: StoryApi,
    private val userApi: UserApi,
    private val siteProvider: SiteProvider,
    private val bodyGenerator: DigestEmailBodyGenerator,
    private val eventStream: EventStream
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DigestDelegate::class.java)
        const val CAMPAIGN = "daily-digest"
    }

    fun invoke(startDate: LocalDate, endDate: LocalDate) {
        val site = siteProvider.get(1L)
        invoke(startDate, endDate, site)
    }

    fun invoke(startDate: LocalDate, endDate: LocalDate, site: Site) {
        val stories = storyApi.published(startDate, endDate, 10, 0).stories
            .filter { it.siteId == site.id }
        if (stories.isEmpty()) {
            LOGGER.info("No story published from $startDate to $endDate")
            return
        }

        var offset = 0
        var count = 0
        while (true) {
            val users = userApi.searchUsers(limit = 100, offset = offset).users
            if (users.isEmpty()) {
                return
            }
            offset += users.size

            users.forEach {
                count += send(startDate, endDate, site, stories, it)
            }
        }
        LOGGER.info("$count email(s) sent")
    }

    private fun send(startDate: LocalDate, endDate: LocalDate, site: Site, stories: List<Story>, user: UserSummary): Int {
        if (user.email.isNullOrEmpty()) {
            LOGGER.warn("User#${user.name} doesn't have an email")
            return 0
        }

        LOGGER.info("Sending to ${user.email}")
        eventStream.publish(
            type = EmailEventType.DELIVERY_SUBMITTED.urn,
            payload = DeliverySubmittedEventPayload(
                request = SendEmailRequest(
                    siteId = stories[0].siteId,
                    subject = "Your Weekly Digest",
                    campaign = CAMPAIGN,
                    contentType = "text/html",
                    sender = Sender(),
                    recipient = Address(
                        email = user.email!!,
                        displayName = user.fullName
                    ),
                    body = bodyGenerator.generate(stories, site, user, startDate, endDate)
                )
            )
        )
        return 1
    }

    private fun locale(user: User): Locale =
        if (user.language.isNullOrEmpty()) Locale("fr") else Locale(user.language)

    private fun enabled(site: Site): Boolean =
        site.attributes.find { SiteAttribute.NEWSLETTER_ENABLED.urn == it.urn }?.value == "true"
}
