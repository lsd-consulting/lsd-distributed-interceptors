package io.lsdconsulting.lsd.distributed.interceptor.integration

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("wrong-connection-string")
@Disabled // TODO Haven't figured out how to assert when app fails to start
class WrongLsdConnectionStringIT: IntegrationTestBase() {

    @Test
    fun `should fail on wrong connection string`() {
    }
}
