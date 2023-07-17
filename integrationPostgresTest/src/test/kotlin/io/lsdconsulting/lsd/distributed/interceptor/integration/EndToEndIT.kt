package io.lsdconsulting.lsd.distributed.interceptor.integration

import com.lsd.core.LsdContext
import com.lsd.core.domain.ParticipantType
import com.lsd.core.domain.Status
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.LsdLogger
import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.net.URISyntaxException
import javax.sql.DataSource

class EndToEndIT : IntegrationTestBase() {
    @Autowired
    private lateinit var testRepository: TestRepository

    @Autowired
    private lateinit var interactionGenerator: InteractionGenerator

    @Autowired
    private lateinit var dataSource: DataSource

    private lateinit var lsdLogger: LsdLogger

    private val lsdContext = LsdContext()
    private val mainTraceId = TraceIdGenerator.generate()
    private val setupTraceId1 = TraceIdGenerator.generate()
    private val setupTraceId2 = TraceIdGenerator.generate()

    @BeforeEach
    fun setup() {
        lsdLogger = LsdLogger(interactionGenerator)
        testRepository.createTable(dataSource)
        testRepository.clearTable(dataSource)
    }

    @Test
    @Throws(URISyntaxException::class)
    fun `should generate lsd with supplied names`() {
        lsdContext.addParticipants(
            listOf(
                ParticipantType.ACTOR.called("Client"),
                ParticipantType.PARTICIPANT.called("TestApp"),
                ParticipantType.QUEUE.called("SomethingDoneEvent"),
                ParticipantType.PARTICIPANT.called("UNKNOWN_TARGET"),
                ParticipantType.PARTICIPANT.called("Downstream")
            )
        )
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp")
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, containsString("response_from_controller"))
        Awaitility.await().untilAsserted {
            assertThat(
                testRepository.findAll(mainTraceId), hasSize(8)
            )
        }
        lsdLogger.captureInteractionsFromDatabase(lsdContext, mainTraceId)
        val report =
            getReport("shouldRecordHeaderSuppliedNames", "Should record header supplied names - Client and TestApp")

        // Assert diagram content
        assertThat(report, containsString("Client -&gt; TestApp"))
        assertThat(report, containsString("GET /api-listener?message=from_test"))
        assertThat(report, containsString("TestApp -&gt; SomethingDoneEvent"))
        assertThat(report, containsString("publish event"))
        assertThat(report, containsString("SomethingDoneEvent -&gt; TestApp"))
        assertThat(report, containsString("consume message"))
        assertThat(report, containsString("TestApp --&gt; Client"))
        assertThat(report, matchesPattern("(?s).*\"sync 200 OK response \\([0-9]+ ms\\)\"(?s).*"))
        assertThat(report, containsString("TestApp -&gt; UNKNOWN_TARGET"))
        assertThat(report, containsString("POST /external-api?message=from_feign"))
        assertThat(report, containsString("UNKNOWN_TARGET --&gt; TestApp"))
        assertThat(report, matchesPattern("(?s).*\"sync 200 OK response \\([0-9]+ ms\\)\"(?s).*"))
        assertThat(report, containsString("TestApp -&gt; Downstream"))
        assertThat(report, containsString("POST /external-api?message=from_feign"))
        assertThat(report, containsString("Downstream --&gt; TestApp"))
        assertThat(report, matchesPattern("(?s).*\"sync 200 OK response \\([0-9]+ ms\\)\"(?s).*"))
    }

    @Test
    @Throws(URISyntaxException::class)
    fun `should generate diagram with supplied names and colours for multiple trace ids`() {
        lsdContext.addParticipants(
            listOf(
                ParticipantType.ACTOR.called("E2E"),
                ParticipantType.PARTICIPANT.called("Setup1"),
                ParticipantType.PARTICIPANT.called("Setup2"),
                ParticipantType.ACTOR.called("Client"),
                ParticipantType.PARTICIPANT.called("TestApp"),
                ParticipantType.QUEUE.called("SomethingDoneEvent"),
                ParticipantType.PARTICIPANT.called("UNKNOWN_TARGET"),
                ParticipantType.PARTICIPANT.called("Downstream")
            )
        )
        givenExternalApi()
        val setup1Response = sentRequest("/setup1", setupTraceId1, "E2E", "Setup1")
        assertThat(setup1Response.statusCode, `is`(HttpStatus.OK))
        Awaitility.await().untilAsserted {
            assertThat(
                testRepository.findAll(setupTraceId1), hasSize(2)
            )
        }
        val response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp")
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        Awaitility.await().untilAsserted {
            assertThat(
                testRepository.findAll(mainTraceId), hasSize(8)
            )
        }
        val setup2Response = sentRequest("/setup2", setupTraceId2, "E2E", "Setup2")
        assertThat(setup2Response.statusCode, `is`(HttpStatus.OK))
        Awaitility.await().untilAsserted {
            assertThat(
                testRepository.findAll(setupTraceId2), hasSize(2)
            )
        }
        lsdLogger.captureInteractionsFromDatabase(
            lsdContext, mapOf(
                mainTraceId to "blue",
                setupTraceId1 to "green",
                setupTraceId2 to "red"
            )
        )
        val report = getReport(
            "shouldGenerateDiagramWithSuppliedNamesAndColoursForMultipleTraceIds",
            "Should generate LSD with supplied names and colours for multiple traceIds"
        )

        // Assert diagram content
        assertThat(report, containsString(mainTraceId))
        assertThat(report, containsString(setupTraceId1))
        assertThat(report, containsString(setupTraceId2))
        assertThat(report, containsString("blue"))
        assertThat(report, containsString("green"))
        assertThat(report, containsString("red"))
    }

    private fun getReport(title: String, description: String): String {
        lsdContext.completeScenario(title, description, Status.SUCCESS)
        val report = lsdContext.renderReport(title)
        lsdContext.completeReport(title)
        return report
    }
}
