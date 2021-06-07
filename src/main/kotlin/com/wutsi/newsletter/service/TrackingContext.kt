package com.wutsi.newsletter.service

import com.wutsi.user.dto.UserSummary
import org.springframework.stereotype.Service
import java.util.UUID

@Service
open class TrackingContext {
    private val hitId = mutableMapOf<Long, String>()
    private val deviceId = mutableMapOf<Long, String>()

    fun hitId(user: UserSummary): String {
        val key = user.id
        if (!hitId.containsKey(key))
            hitId[key] = UUID.randomUUID().toString()

        return hitId[key]!!
    }

    fun deviceId(user: UserSummary): String {
        val key = user.id
        if (!deviceId.containsKey(key))
            deviceId[key] = UUID.randomUUID().toString()

        return deviceId[key]!!
    }
}
