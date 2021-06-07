package com.wutsi.newsletter.service

import com.github.mustachejava.DefaultMustacheFactory
import com.wutsi.site.dto.Site
import com.wutsi.story.dto.Story
import com.wutsi.subscription.SubscriptionApi
import com.wutsi.user.UserApi
import com.wutsi.user.dto.UserSummary
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Entities.EscapeMode.extended
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Service
class NewsletterEmailBodyGenerator(
    @Autowired private val editorJSService: EditorJSService,
    @Autowired private val filters: FilterSet,
    @Autowired private val messageSource: MessageSource,
    @Autowired private val subscriptionApi: SubscriptionApi,
    @Autowired private val userApi: UserApi,
    @Autowired private val trackingContext: TrackingContext
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(NewsletterEmailBodyGenerator::class.java)
    }

    fun generate(story: Story, site: Site, user: UserSummary, campaign: String): String {
        val fullAccess = hasFullAccess(story, user)
        val doc = editorJSService.fromJson(story.content, !fullAccess)
        val html = filter(
            html = editorJSService.toHtml(doc),
            context = FilterContext(
                site = site,
                user = user,
                campaign = campaign
            )
        )
        return merge(story, site, user, html, fullAccess, campaign)
    }

    private fun hasFullAccess(story: Story, user: UserSummary): Boolean {
        if (story.access == "PREMIUM_SUBSCRIBER")
            return hasSubscription(story.userId, user.id)

        if (story.access == "SUBSCRIBER")
            return isFollowedBy(story.userId, user.id)

        return true
    }

    private fun hasSubscription(blogId: Long, userId: Long): Boolean {
        try {
            return subscriptionApi.partnerSubscriptions(blogId, "ACTIVE", userId)
                .subscriptions
                .isNotEmpty()
        } catch (ex: Exception) {
            LOGGER.warn("Unable to resolve subscription for User#$userId on Blog#$blogId", ex)
            return false
        }
    }

    private fun isFollowedBy(blogId: Long, userId: Long): Boolean {
        try {
            return userApi.searchFollowers(
                id = blogId,
                followerUserId = userId,
                limit = 1,
                offset = 0
            )
                .followers
                .isNotEmpty()
        } catch (ex: Exception) {
            LOGGER.warn("Unable find if User#$userId follows Blog#$blogId", ex)
            return false
        }
    }

    private fun merge(story: Story, site: Site, user: UserSummary, content: String, fullAccess: Boolean, campaign: String): String {
        val reader = InputStreamReader(NewsletterEmailBodyGenerator::class.java.getResourceAsStream("/templates/newsletter.html"))
        reader.use {
            val writer = StringWriter()
            writer.use {
                val mustache = DefaultMustacheFactory().compile(reader, "text")
                val scope = scope(story, site, user, content, fullAccess, campaign)
                mustache.execute(
                    writer,
                    mapOf(
                        "scope" to scope
                    )
                )
                writer.flush()
                return writer.toString()
            }
        }
    }

    fun scope(story: Story, site: Site, user: UserSummary, content: String, fullAccess: Boolean, campaign: String): Map<String, Any?> {
        val storyUrl = "${site.websiteUrl}${story.slug}"
        val locale = locale(user)
        return mapOf(
            "pixelUrl" to "${site.websiteUrl}/mail/track/${story.id}.png" +
                "?u=${user.id}" +
                "&d=${story.readingMinutes}" +
                "&c=$campaign" +
                "&hid=${trackingContext.hitId(user)}" +
                "&did=${trackingContext.deviceId(user)}",
            "storyUrl" to storyUrl,
            "title" to StringEscapeUtils.escapeHtml4(story.title),
            "publishedDate" to StringEscapeUtils.escapeHtml4(formatMediumDate(Date(story.publishedDateTime), locale)),
            "content" to content,
            "shareFacebookUrl" to "https://www.facebook.com/sharer/sharer.php?display=page&u=$storyUrl",
            "shareTwitterUrl" to "http://www.twitter.com/intent/tweet?url=$storyUrl",
            "shareLinkedinUrl" to "https://www.linkedin.com/shareArticle?mini=true&url=$storyUrl",
            "subscribe" to subscribeScope(fullAccess, story, site, locale)
        )
    }

    private fun subscribeScope(fullAccess: Boolean, story: Story, site: Site, locale: Locale): Map<String, String>? {
        if (fullAccess)
            return null

        val blog = userApi.getUser(story.userId).user
        return mapOf(
            "title" to messageSource.getMessage(
                if (story.access == "SUBSCRIBER") "subscriber_title" else "premium_title",
                arrayOf(),
                locale
            ),
            "subscribeUrl" to if (story.access == "SUBSCRIBER")
                "${site.websiteUrl}/@/${blog.name}/subscribe"
            else
                "${site.websiteUrl}/@/${blog.name}/subscribe?premium=1",
            "subscribeButton" to messageSource.getMessage(
                "subscribe_button",
                arrayOf(),
                locale
            )
        )
    }

    private fun formatMediumDate(date: Date, locale: Locale): String =
        SimpleDateFormat("d MMM yyyy", locale).format(date)

    private fun locale(user: UserSummary): Locale =
        if (user.language.isNullOrEmpty()) Locale("fr") else Locale(user.language)

    private fun filter(html: String, context: FilterContext): String {
        val doc = filters.filter(Jsoup.parse(html), context)
        doc.outputSettings()
            .charset("ASCII")
            .escapeMode(extended)
        return doc.body().html()
    }
}
