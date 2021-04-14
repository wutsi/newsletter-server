package com.wutsi.newsletter.endpoint

import com.wutsi.newsletter.delegate.ShareDelegate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
public class ShareController(
    private val `delegate`: ShareDelegate
) {
    @GetMapping("/v1/newsletter/share")
    public fun invoke(@RequestParam(name = "story-id", required = false) storyId: Long) {
        delegate.invoke(storyId)
    }
}
