package me.hiten.completion.androidsearch

import me.hiten.completion.Client
import org.apache.http.util.TextUtils
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.util.*
import javax.xml.parsers.SAXParserFactory

object GoogleMavenGroupIdSearcher {


    private var sGroupIds: List<GroupIdBean>? = null


    private var sTempResult: List<GroupIdBean>? = null

    fun matchByWords(world: String?): List<GroupIdBean>? {
        if (TextUtils.isEmpty(world)) {
            return null
        }

        if (sGroupIds == null) {
            requestIndexInfo()
        }

        if (sGroupIds == null || sGroupIds!!.isEmpty()) {
            return null
        }

        if (sTempResult == null) {
            sTempResult = ArrayList(sGroupIds!!)
        }
        if (sTempResult!!.size != sGroupIds!!.size) {
            sTempResult = ArrayList(sGroupIds!!)
        }
        for (groupIdBean in sTempResult!!) {
            groupIdBean.matched = groupIdBean.groupId!!.contains(world!!)
        }
        return sTempResult
    }

    class GroupIdBean {
        var groupId: String? = null
        var matched: Boolean = false
    }

    private fun requestIndexInfo() {
        try {
            var result = Client.get("https://dl.google.com/dl/android/maven2/master-index.xml").trim { it <= ' ' }
            if (result.startsWith("<?xml")) {
                val i = result.indexOf(">")
                result = result.substring(i + 1)
            }
            val saxParserFactory = SAXParserFactory.newInstance()
            val xmlReader = saxParserFactory.newSAXParser().xmlReader
            xmlReader.contentHandler = MyContentHandler()
            xmlReader.parse(InputSource(result.reader()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private class MyContentHandler : DefaultHandler() {

        private var groupIdList: MutableList<GroupIdBean>? = null

        @Throws(SAXException::class)
        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            super.startElement(uri, localName, qName, attributes)
            if ("metadata" == qName) {
                groupIdList = ArrayList()
            }
        }


        @Throws(SAXException::class)
        override fun endElement(uri: String?, localName: String?, qName: String?) {
            super.endElement(uri, localName, qName)
            if (!TextUtils.isEmpty(qName) && "metadata" != qName) {
                val groupIdBean = GroupIdBean()
                groupIdBean.groupId = qName
                groupIdList!!.add(groupIdBean)
            }
        }

        @Throws(SAXException::class)
        override fun endDocument() {
            super.endDocument()
            sGroupIds = groupIdList
            sTempResult = null
        }
    }
}
