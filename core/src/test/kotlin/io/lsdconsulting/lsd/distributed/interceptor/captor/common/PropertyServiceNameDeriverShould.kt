package io.lsdconsulting.lsd.distributed.interceptor.captor.common

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class PropertyServiceNameDeriverShould {

    @Test
    fun `remove specific word from the service name`() {
        val underTest = PropertyServiceNameDeriver("Global User Service")
        assertThat(underTest.serviceName, `is`("Global User"))
    }
}
