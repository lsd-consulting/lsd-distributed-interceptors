package io.lsdconsulting.lsd.distributed.interceptor.integration

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RabbitConfig
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RabbitTemplateConfig
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RestConfig
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
import org.testcontainers.containers.PostgreSQLContainer
import java.net.URI
import java.net.URISyntaxException

private const val POSTGRES_PORT = 5432
private const val POSTGRES_IMAGE = "postgres:15.3-alpine3.18"
private const val TABLE_NAME = "lsd_database"

@Import(RestConfig::class, RabbitConfig::class, RabbitTemplateConfig::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
open class IntegrationTestBase {
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
        private var postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer(POSTGRES_IMAGE)
            .withDatabaseName(TABLE_NAME)
            .withUsername("sa")
            .withPassword("sa")
            .withExposedPorts(POSTGRES_PORT)
            .withCreateContainerCmdModifier { cmd ->
                cmd.withHostConfig(
                    HostConfig().withPortBindings(
                        PortBinding(
                            Ports.Binding.bindPort(POSTGRES_PORT), ExposedPort(
                                POSTGRES_PORT
                            )
                        )
                    )
                )
            }

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            WireMock.configureFor(8070)
            wireMockServer.start()
            postgreSQLContainer.start()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            wireMockServer.stop()
            postgreSQLContainer.stop()
        }
    }
}
