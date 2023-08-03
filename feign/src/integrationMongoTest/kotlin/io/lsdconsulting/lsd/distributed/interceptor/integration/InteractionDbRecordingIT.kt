package io.lsdconsulting.lsd.distributed.interceptor.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.REQUEST
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.RESPONSE
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.integration.matcher.InterceptedInteractionMatcher.Companion.with
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RepositoryConfig
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClient
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClientWithTargetHeader
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.util.*

private const val WIREMOCK_SERVER_PORT = 8070

@Import(RepositoryConfig::class)
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
class InteractionDbRecordingIT {

    @Autowired
    private lateinit var externalClient: ExternalClient

    @Autowired
    private lateinit var externalClientWithTargetHeader: ExternalClientWithTargetHeader

    @Autowired
    private lateinit var testRepository: TestRepository

    @BeforeEach
    fun setup() {
        testRepository.deleteAll()
    }

    @Test
    fun `should record feign client interactions`() {
        val response = externalClient.get("from_test_client")
        assertThat(response, `is`("Response:from_test_client"))

        val interceptedInteractions: MutableList<InterceptedInteraction?> = ArrayList()
        Awaitility.await().untilAsserted {
            val foundInterceptedInteractions = testRepository.findAll("3e316fc2da26a3c7")
            assertThat(foundInterceptedInteractions, hasSize(2))
            interceptedInteractions.addAll(foundInterceptedInteractions)
        }

        // Assert db state
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                with(
                    interactionType = REQUEST,
                    serviceName = "TestApp",
                    body = "",
                    target = "UNKNOWN_TARGET",
                    path = "/get-api?message=from_test_client",
                    httpStatus = null,
                    httpMethod = "GET",
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                with(
                    interactionType = RESPONSE,
                    serviceName = "TestApp",
                    body = "Response:from_test_client",
                    target = "UNKNOWN_TARGET",
                    path = "/get-api?message=from_test_client",
                    httpStatus = "200 OK",
                    httpMethod = null,
                )
            )
        )
    }

    @Test
    fun `should record interactions with supplied names through headers`() {
        val response = externalClientWithTargetHeader.post("from_test_client_with_target_header")
        assertThat(response, `is`("Response:from_test_client_with_target_header"))

        val interceptedInteractions: MutableList<InterceptedInteraction?> = ArrayList()
        Awaitility.await().untilAsserted {
            val foundInterceptedInteractions = testRepository.findAll("3e316fc2da26a3c7")
            assertThat(foundInterceptedInteractions, hasSize(2))
            interceptedInteractions.addAll(foundInterceptedInteractions)
        }

        // Assert db state
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                with(
                    interactionType = REQUEST,
                    serviceName = "Upstream",
                    body = "from_test_client_with_target_header",
                    target = "Downstream",
                    path = "/post-api",
                    httpStatus = null,
                    httpMethod = "POST",
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                with(
                    interactionType = RESPONSE,
                    serviceName = "Upstream",
                    body = "Response:from_test_client_with_target_header",
                    target = "Downstream",
                    path = "/post-api",
                    httpStatus = "200 OK",
                    httpMethod = null,
                )
            )
        )
    }

    companion object {
        private val wireMockServer = WireMockServer(WIREMOCK_SERVER_PORT)

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            WireMock.configureFor(WIREMOCK_SERVER_PORT)
            wireMockServer.start()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            wireMockServer.stop()
        }
    }
}
