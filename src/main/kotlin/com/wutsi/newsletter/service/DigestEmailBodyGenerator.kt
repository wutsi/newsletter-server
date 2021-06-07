package com.wutsi.newsletter.service

import com.github.mustachejava.DefaultMustacheFactory
import com.wutsi.site.dto.Site
import com.wutsi.stats.StatsApi
import com.wutsi.story.dto.Story
import com.wutsi.user.dto.UserSummary
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import java.io.StringWriter
import java.time.LocalDate
import java.util.Locale

@Service
class DigestEmailBodyGenerator(
    private val newsletter: NewsletterEmailBodyGenerator,
    private val statsApi: StatsApi,
    private val messageSource: MessageSource
) {
    fun generate(
        stories: List<Story>,
        site: Site,
        user: UserSummary,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val xstories = filterViewedStories(stories, user, startDate, endDate)
        if (xstories.isEmpty())
            return ""

        val reader = InputStreamReader(DigestEmailBodyGenerator::class.java.getResourceAsStream("/templates/digest.html"))
        reader.use {
            val writer = StringWriter()
            writer.use {
                val mustache = DefaultMustacheFactory().compile(reader, "text")
                val scope = scope(xstories, site, user)
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

    fun scope(stories: List<Story>, site: Site, user: UserSummary): Map<String, Any?> {
        val locale = locale(user)
        return mapOf(
            "greetings" to messageSource.getMessage("digest_greetings", arrayOf(user.fullName), locale),
            "stories" to stories.map {
                mapOf(
                    "anchor" to "a_${it.id}",
                    "title" to it.title,
                    "url" to "${site.websiteUrl}${it.slug}",
                    "content" to newsletter.generate(
                        story = it,
                        site = site,
                        user = user
                    )
                )
            }
        )
    }

    private fun filterViewedStories(stories: List<Story>, user: UserSummary, startDate: LocalDate, endDate: LocalDate): List<Story> {
        val storyIds = statsApi.views(
            startDate = startDate,
            endDate = endDate,
            userId = user.id,
            limit = 200,
            offset = 0
        ).views.map { it.storyId }.toSet()
        return stories.filter { !storyIds.contains(it.id) }.toMutableList()
    }

    private fun locale(user: UserSummary): Locale =
        if (user.language.isNullOrEmpty()) Locale("fr") else Locale(user.language)
}
