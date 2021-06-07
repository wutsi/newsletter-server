package com.wutsi.newsletter.service

import org.jsoup.nodes.Document

open class FilterSet(val filters: List<Filter>) : Filter {
    override fun filter(doc: Document, context: FilterContext): Document {
        var xdoc = doc
        filters.forEach {
            xdoc = it.filter(xdoc, context)
        }
        return xdoc
    }
}
