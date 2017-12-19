package org.molgenis.r

import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.molgenis.test.AbstractMockitoTest
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.util.*

class RScriptExecutorTest : AbstractMockitoTest() {
    @Mock
    private val httpClient: CloseableHttpClient? = null
    @Mock
    private val openCpuSettings: OpenCpuSettings? = null

    private var rScriptExecutor: RScriptExecutor? = null

    private val executeScriptHttpResponse: CloseableHttpResponse
        get() {
            val executeScriptResponse = mock<CloseableHttpResponse>(CloseableHttpResponse::class.java)
            val statusLine = `when`(mock<StatusLine>(StatusLine::class.java).statusCode).thenReturn(201).getMock<StatusLine>()
            `when`(executeScriptResponse.statusLine).thenReturn(statusLine)
            val header = `when`(mock<Header>(Header::class.java).value).thenReturn("sessionId").getMock<Header>()
            `when`(executeScriptResponse.getFirstHeader("X-ocpu-session")).thenReturn(header)
            val executeScriptEntity = mock<HttpEntity>(HttpEntity::class.java)
            `when`(executeScriptResponse.entity).thenReturn(executeScriptEntity)
            return executeScriptResponse
        }

    private val scriptResultHttpResponse: CloseableHttpResponse
        @Throws(IOException::class)
        get() {
            val getScriptResultResponse = mock<CloseableHttpResponse>(CloseableHttpResponse::class.java)
            val statusLine = `when`(mock<StatusLine>(StatusLine::class.java).statusCode).thenReturn(200).getMock<StatusLine>()
            `when`(getScriptResultResponse.statusLine).thenReturn(statusLine)
            val getScriptResultEntity = mock<HttpEntity>(HttpEntity::class.java)
            `when`<InputStream>(getScriptResultEntity.content).thenReturn(ByteArrayInputStream("value".toByteArray(UTF_8)))
            `when`(getScriptResultResponse.entity).thenReturn(getScriptResultEntity)
            return getScriptResultResponse
        }

    @BeforeMethod
    fun setUpBeforeMethod() {
        `when`(openCpuSettings!!.scheme).thenReturn("http")
        `when`(openCpuSettings.host).thenReturn("ocpu.molgenis.org")
        `when`(openCpuSettings.port).thenReturn(80)
        `when`(openCpuSettings.rootPath).thenReturn("/ocpu/")
        rScriptExecutor = RScriptExecutor(httpClient, openCpuSettings)
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun testExecuteScriptValueOutput() {
        val executeScriptResponse = executeScriptHttpResponse
        val getScriptResultResponse = scriptResultHttpResponse
        val requestsCaptor = ArgumentCaptor.forClass<HttpUriRequest, HttpUriRequest>(HttpUriRequest::class.java)
        `when`(httpClient!!.execute(requestsCaptor.capture())).thenReturn(executeScriptResponse, getScriptResultResponse)

        val script = "script"
        val resultValue = rScriptExecutor!!.executeScript(script, null)
        assertEquals(resultValue, "value")

        val requests = requestsCaptor.allValues
        assertEquals(requests[0].uri, URI("http://ocpu.molgenis.org:80/ocpu/library/base/R/identity"))
        assertEquals(requests[1].uri, URI("http://ocpu.molgenis.org:80/ocpu/tmp/sessionId/stdout"))
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun testExecuteScriptFileOutput() {
        val executeScriptResponse = executeScriptHttpResponse
        val getScriptResultResponse = scriptResultHttpResponse
        val requestsCaptor = ArgumentCaptor.forClass<HttpUriRequest, HttpUriRequest>(HttpUriRequest::class.java)
        `when`(httpClient!!.execute(requestsCaptor.capture())).thenReturn(executeScriptResponse, getScriptResultResponse)

        val outputPath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID()
                .toString()
                .replace("-".toRegex(), "")
        val deleted: Boolean
        try {
            val script = "script"
            val resultValue = rScriptExecutor!!.executeScript(script, outputPath)
            assertNull(resultValue)
            assertEquals(Files.readAllBytes(File(outputPath).toPath()), "value".toByteArray(UTF_8))

            val requests = requestsCaptor.allValues
            assertEquals(requests[0].uri, URI("http://ocpu.molgenis.org:80/ocpu/library/base/R/identity"))
            assertTrue(requests[1]
                    .uri
                    .toString()
                    .matches("http://ocpu.molgenis.org:80/ocpu/tmp/sessionId/files/[a-z0-9]+".toRegex()))
        } finally {
            deleted = File(outputPath).delete()
        }
        if (!deleted) {
            throw IOException(String.format("Cannot delete '%s'", outputPath))
        }
    }
}