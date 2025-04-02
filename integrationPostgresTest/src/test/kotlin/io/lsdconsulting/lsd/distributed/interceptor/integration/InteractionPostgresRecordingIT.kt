package io.lsdconsulting.lsd.distributed.interceptor.integration

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator
import io.lsdconsulting.lsd.distributed.interceptor.integration.matcher.InterceptedInteractionMatcher
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller.event.SomethingDoneEvent
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository
import org.apache.commons.lang3.RandomStringUtils.secure
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import java.net.URISyntaxException
import java.util.*
import javax.sql.DataSource

class InteractionPostgresRecordingIT: IntegrationPostgresTestBase() {
    @Autowired
    private lateinit var testRepository: TestRepository

    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    private lateinit var dataSource: DataSource

    private val mainTraceId = TraceIdGenerator.generate()
    private val sourceName = secure().nextAlphanumeric(10).uppercase(Locale.getDefault())
    private val targetName = secure().nextAlphanumeric(10).uppercase(Locale.getDefault())

    @BeforeEach
    fun setup() {
        testRepository.createTable(dataSource)
        testRepository.clearTable(dataSource)
    }

    @Test
    @DisplayName("Should record interactions from RestTemplate, FeignClient and RabbitListener")
    @Throws(URISyntaxException::class)
    fun `should record rest template feign client and listener interactions`() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, null, null)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, `is`("response_from_controller"))
        val interceptedInteractions: MutableList<InterceptedInteraction?> = ArrayList()
        await().untilAsserted {
            val foundInterceptedInteractions = testRepository.findAll(mainTraceId)
            assertThat(foundInterceptedInteractions, hasSize(8))
            interceptedInteractions.addAll(foundInterceptedInteractions)
        }

        // Assert db state
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.REQUEST,
                    "TestApp",
                    NO_BODY,
                    "/api-listener?message=from_test",
                    "/api-listener?message=from_test"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.RESPONSE,
                    "TestApp",
                    "response_from_controller",
                    "/api-listener?message=from_test",
                    "/api-listener?message=from_test"
                )
            )
        )
        assertThat(
            "PUBLISH interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.PUBLISH,
                    "TestApp",
                    "{\"message\":\"from_controller\"}",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent"
                )
            )
        )
        assertThat(
            "CONSUMER interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.CONSUME,
                    "TestApp",
                    "{\"message\":\"from_controller\"}",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.REQUEST,
                    "TestApp",
                    "from_listener",
                    "UNKNOWN_TARGET",
                    "/external-api?message=from_feign"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.RESPONSE,
                    "TestApp",
                    "from_external",
                    "UNKNOWN_TARGET",
                    "/external-api?message=from_feign"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.REQUEST,
                    "TestApp",
                    "from_listener",
                    "Downstream",
                    "/external-api?message=from_feign"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.RESPONSE,
                    "TestApp",
                    "from_external",
                    "Downstream",
                    "/external-api?message=from_feign"
                )
            )
        )
    }

    @Test
    @DisplayName("Should record interactions with supplied names through headers")
    @Throws(URISyntaxException::class)
    fun `should record header supplied names`() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, sourceName, targetName)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, containsString("response_from_controller"))
        val interceptedInteractions: MutableList<InterceptedInteraction?> = ArrayList()
        await().untilAsserted {
            val foundInterceptedInteractions = testRepository.findAll(mainTraceId)
            assertThat(foundInterceptedInteractions, hasSize(8))
            interceptedInteractions.addAll(foundInterceptedInteractions)
        }

        // Assert db state
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.REQUEST,
                    sourceName,
                    NO_BODY,
                    targetName,
                    "/api-listener?message=from_test"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.RESPONSE,
                    sourceName,
                    "response_from_controller",
                    targetName,
                    "/api-listener?message=from_test"
                )
            )
        )
        assertThat(
            "PUBLISH interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.PUBLISH,
                    "TestApp",
                    "{\"message\":\"from_controller\"}",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent"
                )
            )
        )
        assertThat(
            "CONSUMER interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.CONSUME,
                    "TestApp",
                    "{\"message\":\"from_controller\"}",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.REQUEST,
                    "TestApp",
                    "from_listener",
                    "UNKNOWN_TARGET",
                    "/external-api?message=from_feign"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.RESPONSE,
                    "TestApp",
                    "from_external",
                    "UNKNOWN_TARGET",
                    "/external-api?message=from_feign"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.REQUEST,
                    "TestApp",
                    "from_listener",
                    "Downstream",
                    "/external-api?message=from_feign"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.RESPONSE,
                    "TestApp",
                    "from_external",
                    "Downstream",
                    "/external-api?message=from_feign"
                )
            )
        )
    }

    @Test
    @Throws(URISyntaxException::class)
    fun `should record receiving messages with rabbit template`() {
        val response = sentRequest("/api-rabbit-template", mainTraceId, sourceName, targetName)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, `is`("response_from_controller"))
        val type = object : ParameterizedTypeReference<SomethingDoneEvent?>() {}
        await().untilAsserted {
            val message = rabbitTemplate.receiveAndConvert("queue-rabbit-template", 2000, type)
            assertThat(message, `is`(notNullValue()))
            assertThat(message?.message, `is`("from_controller"))
        }
        val interceptedInteractions: MutableList<InterceptedInteraction?> = ArrayList()
        await().untilAsserted {
            val foundInterceptedInteractions = testRepository.findAll(mainTraceId)
            assertThat(foundInterceptedInteractions, hasSize(4))
            interceptedInteractions.addAll(foundInterceptedInteractions)
        }
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.REQUEST,
                    sourceName,
                    NO_BODY,
                    targetName,
                    "/api-rabbit-template?message=from_test"
                )
            )
        )
        assertThat(
            "REQUEST interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.RESPONSE,
                    sourceName,
                    "response_from_controller",
                    targetName,
                    "/api-rabbit-template?message=from_test"
                )
            )
        )
        assertThat(
            "PUBLISH interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.PUBLISH,
                    "TestApp",
                    "{\"message\":\"from_controller\"}",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent"
                )
            )
        )
        assertThat(
            "CONSUMER interaction missing",
            interceptedInteractions,
            hasItem(
                InterceptedInteractionMatcher.with(
                    InteractionType.CONSUME,
                    "TestApp",
                    "{\"message\":\"from_controller\"}",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent"
                )
            )
        )
    }

    @Test
    @Throws(URISyntaxException::class)
    fun `should record obfuscated headers`() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, null, null)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        val interceptedInteractions = mutableListOf<InterceptedInteraction>()
        await().untilAsserted {
            val foundInterceptedInteractions = testRepository.findAll(mainTraceId)
            assertThat(foundInterceptedInteractions, hasSize(8))
            interceptedInteractions.addAll(foundInterceptedInteractions)
        }
        assertThat(
            "Header obfuscation did not work ",
            interceptedInteractions,
            not(hasItem(hasProperty<Any>("requestHeaders", hasEntry("Authorization", listOf("Password")))))
        )
    }

    companion object {
        private const val NO_BODY = ""
    }
}
