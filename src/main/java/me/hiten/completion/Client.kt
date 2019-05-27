package me.hiten.completion

import com.intellij.util.net.IdeHttpClientHelpers
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.util.concurrent.TimeUnit

object Client {

    fun get(uri: String): String {
        println("get request:$uri")
        val clientBuilder = RequestConfig.custom()
        IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(clientBuilder, uri)

        val response: CloseableHttpResponse = try {
            HttpClients.custom()
                    .setDefaultRequestConfig(clientBuilder.build())
                    .setConnectionTimeToLive(1, TimeUnit.SECONDS)
                    .build()
                    .execute(HttpGet(uri))
        } catch (e: Exception) {
            return ""
        }

        return when (response.statusLine.statusCode) {
            200 -> response.entity.content.reader().readText()
            else -> ""
        }
    }
}