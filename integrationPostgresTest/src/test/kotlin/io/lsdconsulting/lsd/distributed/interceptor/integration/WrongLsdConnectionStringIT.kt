package io.lsdconsulting.lsd.distributed.interceptor.integration

import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("wrong-connection-string")
@Disabled // TODO Haven't figured out how to assert when app fails to start
class WrongLsdConnectionStringIT: IntegrationTestBase() {

    @Test
    fun `should fail on wrong connection string`() {
    }
}
