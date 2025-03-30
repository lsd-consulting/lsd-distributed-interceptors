package io.lsdconsulting.lsd.distributed.interceptor.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.*
import lsd.logging.log
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import java.net.URI
import java.net.URISyntaxException

@Import(
    RepositoryConfig::class,
    RestConfig::class,
    RabbitConfig::class,
    RabbitTemplateConfig::class,
    TracingConfig::class
)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
class IntegrationTestBase {
    @LocalServerPort
    private val serverPort = 0

    @Autowired
    private val testRestTemplate: TestRestTemplate? = null
    fun givenExternalApi() {
        WireMock.stubFor(
            WireMock.post(WireMock.urlEqualTo("/external-api?message=from_feign"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody("from_external")
                )
        )
    }

    @Throws(URISyntaxException::class)
    fun sentRequest(
        resourceName: String,
        traceId: String,
        sourceName: String?,
        targetName: String?
    ): ResponseEntity<String> {
        log().info("Sending traceId:{}", traceId)
        val requestEntity: RequestEntity<*> =
            RequestEntity.get(URI("http://localhost:$serverPort$resourceName?message=from_test"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("b3", "$traceId-$traceId-1")
                .header("Source-Name", sourceName)
                .header("Target-Name", targetName)
                .header("Authorization", "Basic password")
                .build()
        return testRestTemplate!!.exchange(requestEntity, String::class.java)
    }

    companion object {
        private val wireMockServer = WireMockServer(8070)

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            WireMock.configureFor(8070)
            wireMockServer.start()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            wireMockServer.stop()
        }
    }
}
