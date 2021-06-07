package com.wutsi.newsletter.event

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.newsletter.delegate.DigestDelegate
import com.wutsi.newsletter.delegate.ShareDelegate
import com.wutsi.story.event.StoryEventType
import com.wutsi.stream.Event
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class EventHandlerTest {
    private lateinit var shareDelegate: ShareDelegate
    private lateinit var digestDelegate: DigestDelegate
    private lateinit var eventHandler: EventHandler

    @BeforeEach
    fun setUp() {
        shareDelegate = mock()
        digestDelegate = mock()
        eventHandler = EventHandler(shareDelegate, digestDelegate)
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

    @Test
    fun `handle digest event`() {
        val event = Event(
            type = NewsletterEventType.DIGEST_REQUEST_SENT.urn,
            payload = """
                {
                }
            """.trimIndent()
        )
        eventHandler.onEvent(event)

        verify(digestDelegate).invoke(LocalDate.now().minusDays(7), LocalDate.now())
    }
}
