package com.wutsi.newsletter.endpoint

import com.wutsi.newsletter.delegate.DigestDelegate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
public class DigestController(
    private val `delegate`: DigestDelegate
) {
    @GetMapping("/v1/newsletter/digest")
    @PreAuthorize(value = "hasAuthority('newsletter')")
    public fun invoke(
        @RequestParam(name = "start-date", required = true)
        startDate: LocalDate,

        @RequestParam(name = "end-date", required = true)
        endDate: LocalDate
    ) {
        delegate.invoke(startDate, endDate)
    }
}
