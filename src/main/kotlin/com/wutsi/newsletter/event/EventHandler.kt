package com.wutsi.newsletter.event

import com.wutsi.newsletter.delegate.ShareDelegate
import com.wutsi.story.event.StoryEventPayload
import com.wutsi.story.event.StoryEventType
import com.wutsi.stream.Event
import com.wutsi.stream.ObjectMapperBuilder
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class EventHandler(
    private val shareDelegate: ShareDelegate
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventHandler::class.java)
    }

    @EventListener
    fun onEvent(event: Event) {
        LOGGER.info("onEvent(event)")

        if (event.type == StoryEventType.PUBLISHED.urn) {
            val payload = ObjectMapperBuilder().build().readValue(event.payload, StoryEventPayload::class.java)
            shareDelegate.invoke(payload.storyId)
        } else {
            LOGGER.info("Event Ignored")
        }
    }
}