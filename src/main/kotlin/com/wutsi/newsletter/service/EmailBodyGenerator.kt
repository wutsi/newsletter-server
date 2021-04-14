package com.wutsi.newsletter.service

import com.github.mustachejava.DefaultMustacheFactory
import com.wutsi.newsletter.delegate.ShareDelegate
import com.wutsi.site.dto.Site
import com.wutsi.story.dto.Story
import com.wutsi.user.dto.User
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Entities.EscapeMode.extended
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import java.io.StringWriter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Service
class EmailBodyGenerator(
    @Autowired private val editorJSService: EditorJSService,
    @Autowired private val filters: FilterSet,
    @Autowired private val messageSource: MessageSource,
    @Value("\${wutsi.tracking-url}") private val trackingUrl: String,
    @Value("\${wutsi.asset-url}") private val assetUrl: String
) {
    fun generate(story: Story, site: Site, user: User): String {
        val doc = editorJSService.fromJson(story.content)
        val html = filter(editorJSService.toHtml(doc))
        return merge(story, site, user, html)
    }

    private fun merge(story: Story, site: Site, user: User, content: String): String {
        val reader = InputStreamReader(EmailBodyGenerator::class.java.getResourceAsStream("/templates/newsletter.html"))
        reader.use {
            val writer = StringWriter()
            writer.use {
                val mustache = DefaultMustacheFactory().compile(reader, "text")
                val scope = scope(story, site, user, content)
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

    fun scope(story: Story, site: Site, user: User, content: String): Map<String, String> {
        val storyUrl = "${site.websiteUrl}${story.slug}"
        val userUrl = "${site.websiteUrl}/@/${user.name}"
        val locale = locale(user)
        return mapOf(
            "pixelUrl" to "$trackingUrl/mail/pixel/${story.id}.png?u=${user.id}&d=${story.readingMinutes}&c=${ShareDelegate.CAMPAIGN}",
            "storyUrl" to storyUrl,
            "title" to StringEscapeUtils.escapeHtml4(story.title),
            "publishedDate" to StringEscapeUtils.escapeHtml4(formatMediumDate(story.publishedDateTime, locale)),
            "content" to content,
            "likeUrl" to "$storyUrl?like=1",
            "likeIconUrl" to "$assetUrl/img/like.png",
            "commentUrl" to "$storyUrl?comment=1",
            "commentIconUrl" to "$assetUrl/img/comment.png",
            "shareUrl" to "$storyUrl?share=1",
            "shareText" to messageSource.getMessage(
                "share_text",
                arrayOf(userUrl, StringEscapeUtils.escapeHtml4(user.fullName)),
                locale
            ),
            "shareButton" to messageSource.getMessage("share_button", arrayOf(), locale)
        )
    }

    private fun formatMediumDate(date: OffsetDateTime, locale: Locale): String =
        date.format(DateTimeFormatter.ofPattern("d MMM uuuu", locale))

    private fun locale(user: User): Locale =
        if (user.language.isNullOrEmpty()) Locale("fr") else Locale(user.language)

    private fun filter(html: String): String {
        val doc = filters.filter(Jsoup.parse(html))
        doc.outputSettings()
            .charset("ASCII")
            .escapeMode(extended)
        return doc.body().html()
    }
}
