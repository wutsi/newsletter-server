package com.wutsi.newsletter.service

import com.wutsi.site.dto.Site
import com.wutsi.user.dto.UserSummary

data class FilterContext(
    val campaign: String = "",
    val site: Site = Site(),
    val user: UserSummary = UserSummary()
)
