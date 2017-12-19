package org.molgenis.r

import org.apache.http.NameValuePair
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.molgenis.script.ScriptException
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.UncheckedIOException
import java.lang.String.format
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.Objects.requireNonNull

/**
 * Executes an R script using OpenCPU
 */
@Service
class RScriptExecutor(httpClient: CloseableHttpClient, openCpuSettings: OpenCpuSettings) {
    private val httpClient: CloseableHttpClient
    private val openCpuSettings: OpenCpuSettings

    private val scriptExecutionUri: URI
        get() {
            try {
                return URI(openCpuUri.toString() + "library/base/R/identity")
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }

        }

    private val openCpuUri: URI
        get() {
            try {
                return URIBuilder().setScheme(openCpuSettings.scheme)
                        .setHost(openCpuSettings.host)
                        .setPort(openCpuSettings.port)
                        .setPath(openCpuSettings.rootPath)
                        .build()
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }

        }

    init {
        this.httpClient = requireNonNull(httpClient)
        this.openCpuSettings = requireNonNull(openCpuSettings)
    }

    /**
     * Execute R script and parse response:
     * - write the response to outputPathname if outputPathname is not null
     * - else return the response
     *
     * @param script         R script to execute
     * @param outputPathname optional output pathname for output file
     * @return response value or null in outputPathname is not null
     */
    internal fun executeScript(script: String, outputPathname: String?): String? {
        var script = script
        // Workaround: script contains the absolute output pathname in case outputPathname is not null
        // Replace the absolute output pathname with a relative filename such that OpenCPU can handle the script.
        val scriptOutputFilename: String?
        if (outputPathname != null) {
            scriptOutputFilename = generateRandomString()
            script = script.replace(outputPathname, scriptOutputFilename)
        } else {
            scriptOutputFilename = null
        }

        try {
            // execute script and use session key to retrieve script response
            val openCpuSessionKey = executeScriptExecuteRequest(script)
            return executeScriptGetResponseRequest(openCpuSessionKey, scriptOutputFilename, outputPathname)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }

    /**
     * Execute R script using OpenCPU
     *
     * @param rScript R script
     * @return OpenCPU session key
     * @throws IOException if error occured during script execution request
     */
    @Throws(IOException::class)
    private fun executeScriptExecuteRequest(rScript: String): String {
        val uri = scriptExecutionUri
        val httpPost = HttpPost(uri)
        val nameValuePair = BasicNameValuePair("x", rScript)
        httpPost.entity = UrlEncodedFormEntity(listOf<NameValuePair>(nameValuePair))

        var openCpuSessionKey: String
        httpClient.execute(httpPost).use { response ->
            val statusCode = response.getStatusLine().getStatusCode()
            if (statusCode >= 200 && statusCode < 300) {
                val openCpuSessionKeyHeader = response.getFirstHeader("X-ocpu-session") ?: throw IOException("Missing 'X-ocpu-session' header")
                openCpuSessionKey = openCpuSessionKeyHeader.getValue()
                EntityUtils.consume(response.getEntity())
            } else if (statusCode == 400) {
                val entity = response.getEntity()
                val rErrorMessage = EntityUtils.toString(entity)
                EntityUtils.consume(entity)
                throw ScriptException(rErrorMessage)
            } else {
                throw ClientProtocolException(format("Unexpected response status: %d", statusCode))
            }
        }
        return openCpuSessionKey
    }

    /**
     * Retrieve R script response using OpenCPU
     *
     * @param openCpuSessionKey    OpenCPU session key
     * @param scriptOutputFilename R script output filename (can be null)
     * @param outputPathname       output pathname (can be null)
     * @return response value or null if scriptOutputFilename is not null
     * @throws IOException if error occured during script response retrieval
     */
    @Throws(IOException::class)
    private fun executeScriptGetResponseRequest(openCpuSessionKey: String, scriptOutputFilename: String?,
                                                outputPathname: String?): String? {
        val responseValue: String?
        if (scriptOutputFilename != null) {
            executeScriptGetFileRequest(openCpuSessionKey, scriptOutputFilename, outputPathname)
            responseValue = null
        } else {
            responseValue = executeScriptGetValueRequest(openCpuSessionKey)
        }
        return responseValue
    }

    /**
     * Retrieve R script file response using OpenCPU and write to file
     *
     * @param openCpuSessionKey    OpenCPU session key
     * @param scriptOutputFilename R script output filename
     * @param outputPathname       Output pathname
     * @throws IOException if error occured during script response retrieval
     */
    @Throws(IOException::class)
    private fun executeScriptGetFileRequest(openCpuSessionKey: String, scriptOutputFilename: String,
                                            outputPathname: String?) {
        val scriptGetValueResponseUri = getScriptGetFileResponseUri(openCpuSessionKey, scriptOutputFilename)
        val httpGet = HttpGet(scriptGetValueResponseUri)
        httpClient.execute(httpGet).use { response ->
            val statusCode = response.getStatusLine().getStatusCode()
            if (statusCode >= 200 && statusCode < 300) {
                val entity = response.getEntity()
                Files.copy(entity.getContent(), Paths.get(outputPathname))
                EntityUtils.consume(entity)
            } else {
                throw ClientProtocolException(format("Unexpected response status: %d", statusCode))
            }
        }
    }

    /**
     * Retrieve and return R script STDOUT response using OpenCPU
     *
     * @param openCpuSessionKey OpenCPU session key
     * @return R script STDOUT
     * @throws IOException if error occured during script response retrieval
     */
    @Throws(IOException::class)
    private fun executeScriptGetValueRequest(openCpuSessionKey: String): String {
        val scriptGetValueResponseUri = getScriptGetValueResponseUri(openCpuSessionKey)
        val httpGet = HttpGet(scriptGetValueResponseUri)
        var responseValue: String
        httpClient.execute(httpGet).use { response ->
            val statusCode = response.getStatusLine().getStatusCode()
            if (statusCode >= 200 && statusCode < 300) {
                val entity = response.getEntity()
                responseValue = EntityUtils.toString(entity)
                EntityUtils.consume(entity)
            } else {
                throw ClientProtocolException(format("Unexpected response status: %d", statusCode))
            }
        }
        return responseValue
    }

    private fun generateRandomString(): String {
        return UUID.randomUUID().toString().replace("-".toRegex(), "")
    }

    private fun getScriptGetValueResponseUri(openCpuSessionKey: String): URI {
        try {
            return URI(openCpuUri.toString() + "tmp/" + openCpuSessionKey + "/stdout")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

    }

    private fun getScriptGetFileResponseUri(openCpuSessionKey: String, fileId: String): URI {
        try {
            return URI(openCpuUri.toString() + "tmp/" + openCpuSessionKey + "/files/" + fileId)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

    }
}
