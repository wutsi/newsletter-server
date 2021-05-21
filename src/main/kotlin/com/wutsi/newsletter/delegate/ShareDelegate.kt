package com.wutsi.newsletter.`delegate`

import com.wutsi.email.dto.Address
import com.wutsi.email.dto.SendEmailRequest
import com.wutsi.email.dto.Sender
import com.wutsi.email.event.DeliverySubmittedEventPayload
import com.wutsi.email.event.EmailEventType
import com.wutsi.newsletter.service.EmailBodyGenerator
import com.wutsi.site.SiteApi
import com.wutsi.site.SiteAttribute
import com.wutsi.site.dto.Site
import com.wutsi.story.StoryApi
import com.wutsi.story.dto.Story
import com.wutsi.stream.EventStream
import com.wutsi.user.UserApi
import com.wutsi.user.dto.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
public class ShareDelegate(
    @Autowired private val siteApi: SiteApi,
    @Autowired private val storyApi: StoryApi,
    @Autowired private val userApi: UserApi,
    @Autowired private val bodyGenerator: EmailBodyGenerator,
    @Autowired private val eventStream: EventStream
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ShareDelegate::class.java)
        const val CAMPAIGN = "newsletter"
    }

    fun invoke(storyId: Long) {
        val story = storyApi.get(storyId).story
        val site = siteApi.get(story.siteId).site
        if (!enabled(site)) {
            LOGGER.warn("Site#${story.siteId} doesn't support newsletter. Request ignored")
            return
        }

        val sender = userApi.get(story.userId).user
        val count = send(story, site, sender)
        LOGGER.info("$count email(s) sent to the follower of User#${story.userId}")
    }

    private fun send(story: Story, site: Site, sender: User): Int {
        var count = 0
        var offset = 0
        val limit = 100
        while (true) {
            val followers = userApi.followers(id = story.userId, limit = limit, offset = offset).followers
            followers.forEach {
                try {
                    count += send(story, site, sender, it.followerUserId)
                } catch (ex: Exception) {
                    LOGGER.warn("Unable to send Story#${story.id} to User#${it.followerUserId}", ex)
                }
            }

            if (followers.size < limit)
                break
            else
                offset += limit
        }
        return count
    }

    private fun send(story: Story, site: Site, sender: User, followerUserId: Long): Int {
        val follower = userApi.get(followerUserId).user
        if (follower.email.isNullOrEmpty()) {
            LOGGER.warn("User#${follower.id} doesn't have an email")
            return 0
        }

        LOGGER.info("Sending story via email to User#${follower.id} at ${follower.email}")
        eventStream.publish(
            type = EmailEventType.DELIVERY_SUBMITTED.urn,
            payload = DeliverySubmittedEventPayload(
                request = SendEmailRequest(
                    siteId = site.id,
                    subject = story.title,
                    campaign = CAMPAIGN,
                    contentType = "text/html",
                    sender = Sender(
                        userId = sender.id,
                        fullName = sender.fullName
                    ),
                    recipient = Address(
                        email = follower.email!!,
                        displayName = follower.fullName
                    ),
                    body = bodyGenerator.generate(story, site, follower)
                )
            )
        )
        return 1
    }

    private fun enabled(site: Site): Boolean =
        site.attributes.find { SiteAttribute.NEWSLETTER_ENABLED.urn == it.urn }?.value == "true"
}
