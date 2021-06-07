package com.wutsi.newsletter.event

import com.wutsi.newsletter.delegate.DigestDelegate
import com.wutsi.newsletter.delegate.ShareDelegate
import com.wutsi.story.event.StoryEventPayload
import com.wutsi.story.event.StoryEventType
import com.wutsi.stream.Event
import com.wutsi.stream.ObjectMapperBuilder
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EventHandler(
    private val shareDelegate: ShareDelegate,
    private val digestDelegate: DigestDelegate
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventHandler::class.java)
    }

    @EventListener
    fun onEvent(event: Event) {
        LOGGER.info("onEvent(${event.type}, ...)")

        if (event.type == StoryEventType.PUBLISHED.urn) {
            val payload = ObjectMapperBuilder().build().readValue(event.payload, StoryEventPayload::class.java)
            shareDelegate.invoke(payload.storyId)
        } else if (event.type == NewsletterEventType.DIGEST_REQUEST_SENT.urn) {
            val now = LocalDate.now()
            digestDelegate.invoke(now.minusDays(7), now)
        } else {
            LOGGER.info("Event Ignored")
        }
    }
}
