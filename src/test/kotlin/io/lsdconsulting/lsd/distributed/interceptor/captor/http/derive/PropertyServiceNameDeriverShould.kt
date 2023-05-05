package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PropertyServiceNameDeriverShould {

    @ParameterizedTest
    @CsvSource(value = ["Global User Service, GlobalUser", "User Address, UserAddress"])
    fun deriveServiceName(appName: String, expectedServiceName: String) {
        val underTest = PropertyServiceNameDeriver(appName)
        assertThat(underTest.serviceName, `is`(expectedServiceName))
    }
}
