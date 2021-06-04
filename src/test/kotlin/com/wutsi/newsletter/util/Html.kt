package com.wutsi.newsletter.util

object Html {
    fun sanitizeHtml(html: String): String =
        html.replace("\\s+".toRegex(), " ")
            .replace(">\\s*".toRegex(), ">")
            .replace("\\s*<".toRegex(), "<")
            .trimIndent()
            .trim()
}
