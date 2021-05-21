package com.wutsi.newsletter.service

import com.wutsi.editorjs.dom.EJSDocument
import com.wutsi.editorjs.html.EJSHtmlReader
import com.wutsi.editorjs.html.EJSHtmlWriter
import com.wutsi.editorjs.json.EJSJsonReader
import com.wutsi.editorjs.json.EJSJsonWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.StringWriter

@Service
class EditorJSService(
    @Autowired private val htmlWriter: EJSHtmlWriter,
    @Autowired private val htmlReader: EJSHtmlReader,
    @Autowired private val jsonReader: EJSJsonReader,
    @Autowired private val jsonWriter: EJSJsonWriter
) {
    fun fromJson(json: String?, summary: Boolean): EJSDocument {
        if (json == null || json.isEmpty())
            return EJSDocument()

        return jsonReader.read(json, false)
    }

    fun toJson(doc: EJSDocument): String {
        val json = StringWriter()
        jsonWriter.write(doc, json)
        return json.toString()
    }

    fun fromHtml(html: String) = htmlReader.read(html)

    fun toHtml(doc: EJSDocument): String {
        val writer = StringWriter()
        htmlWriter.write(doc, writer)
        return writer.toString()
    }
}
