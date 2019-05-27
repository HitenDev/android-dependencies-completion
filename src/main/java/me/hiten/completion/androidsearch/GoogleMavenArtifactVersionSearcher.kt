package me.hiten.completion.androidsearch

import me.hiten.completion.Client
import org.apache.http.util.TextUtils
import org.gradle.internal.impldep.com.amazonaws.util.StringInputStream
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStreamReader
import java.util.*
import javax.xml.parsers.SAXParserFactory

object GoogleMavenArtifactVersionSearcher {


    private var sArtifactVersionMap: MutableMap<String, List<ArtifactVersionBean>>? = null


    fun searchArtifact(groupId: String): List<String>? {
        if (sArtifactVersionMap == null || !sArtifactVersionMap!!.containsKey(groupId)) {
            refreshIndexInfo(groupId)
        }
        if (sArtifactVersionMap == null) {
            return null
        }
        if (sArtifactVersionMap!!.containsKey(groupId)) {
            val list = ArrayList<String>()
            val artifactVersionBeans = sArtifactVersionMap!![groupId]
            if (artifactVersionBeans != null && artifactVersionBeans.isNotEmpty()) {
                for (artifactVersionBean in artifactVersionBeans) {
                    list.add(artifactVersionBean.artifact!!)
                }
            }
            return list
        }
        return null
    }

    fun searchVersion(groupId: String, artifactId: String): List<String>? {
        if (sArtifactVersionMap == null || !sArtifactVersionMap!!.containsKey(groupId)) {
            refreshIndexInfo(groupId)
        }
        if (sArtifactVersionMap == null) {
            return null
        }

        if (sArtifactVersionMap!!.containsKey(groupId)) {
            val artifactVersionBeans = sArtifactVersionMap!![groupId]
            if (artifactVersionBeans != null && artifactVersionBeans.isNotEmpty()) {
                val list = ArrayList<String>()
                for (artifactVersionBean in artifactVersionBeans) {
                    if (artifactId == artifactVersionBean.artifact) {
                        for (version in artifactVersionBean.versions!!) {
                            list.add(version)
                        }
                    }
                }
                return list
            }
        }
        return null
    }

    class ArtifactVersionBean {
        var artifact: String? = null
        var versions: MutableList<String>? = null
    }


    private fun refreshIndexInfo(groupId: String) {
        try {
            if (TextUtils.isEmpty(groupId)) {
                return
            }
            val groupPath = groupId.replace("\\.".toRegex(), "/")
            var result = Client.get("https://dl.google.com/dl/android/maven2/$groupPath/group-index.xml").trim { it <= ' ' }
            if (TextUtils.isEmpty(result)) {
                return
            }
            if (result.startsWith("<?xml")) {
                val i = result.indexOf(">")
                result = result.substring(i + 1)
            }
            println(result)
            val saxParserFactory = SAXParserFactory.newInstance()
            val xmlReader = saxParserFactory.newSAXParser().xmlReader
            xmlReader.contentHandler = MyContentHandler(groupId)
            xmlReader.parse(InputSource(InputStreamReader(StringInputStream(result))))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private class MyContentHandler(private val groupId: String) : DefaultHandler() {

        private var artifactVersionBeanList: MutableList<ArtifactVersionBean>? = null

        @Throws(SAXException::class)
        override fun startDocument() {
            super.startDocument()
            if (sArtifactVersionMap == null) {
                sArtifactVersionMap = HashMap()
            }
        }

        @Throws(SAXException::class)
        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            super.startElement(uri, localName, qName, attributes)
            if (this.groupId == qName) {
                artifactVersionBeanList = ArrayList()
                sArtifactVersionMap!![this.groupId] = artifactVersionBeanList!!
            } else if (artifactVersionBeanList != null) {
                val artifactVersionBean = ArtifactVersionBean()
                artifactVersionBean.artifact = qName
                val versions = attributes!!.getValue("versions")
                if (!TextUtils.isEmpty(versions)) {
                    val split = versions.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (split.isNotEmpty()) {
                        artifactVersionBean.versions = ArrayList()
                        val list = Arrays.asList(*split)
                        list.reverse()
                        artifactVersionBean.versions!!.addAll(list)
                    }
                }
                artifactVersionBeanList!!.add(artifactVersionBean)
            }
        }

        @Throws(SAXException::class)
        override fun endDocument() {
            super.endDocument()
        }
    }
}
