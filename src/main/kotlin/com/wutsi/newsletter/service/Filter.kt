package com.wutsi.newsletter.service

import org.jsoup.nodes.Document

interface Filter {
    fun filter(doc: Document): Document
}
