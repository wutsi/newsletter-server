package com.wutsi.newsletter.event

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.newsletter.delegate.ShareDelegate
import com.wutsi.story.event.StoryEventType
import com.wutsi.stream.Event
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class EventHandlerTest {
    private lateinit var shareDelegate: ShareDelegate
    private lateinit var eventHandler: EventHandler

    @BeforeEach
    fun setUp() {
        shareDelegate = mock()
        eventHandler = EventHandler(shareDelegate)
    }

    @Test
    fun `handle publish event`() {
        val event = Event(
            type = StoryEventType.PUBLISHED.urn,
            payload = """
                {
                    "storyId": 1
                }
            """.trimIndent()
        )
        eventHandler.onEvent(event)

        verify(shareDelegate).invoke(1)
    }
}
