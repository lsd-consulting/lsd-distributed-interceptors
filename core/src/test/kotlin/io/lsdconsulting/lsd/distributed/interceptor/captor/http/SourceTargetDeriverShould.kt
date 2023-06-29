package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SourceTargetDeriverShould {
    private val propertyServiceNameDeriver = mockk<PropertyServiceNameDeriver>()

    private val underTest = SourceTargetDeriver(propertyServiceNameDeriver)

    private val appName = randomAlphabetic(20)
    private val sourceName = randomAlphabetic(20)
    private val targetName = randomAlphabetic(20)
    private val path = randomAlphabetic(20)

    @BeforeEach
    fun setup() {
        every { propertyServiceNameDeriver.serviceName }  returns appName
        underTest.initialise()
    }

    @Test
    fun `use header for source name`() {
        val result = underTest.deriveServiceName(mapOf("Source-Name" to listOf(sourceName)))
        assertThat(result, `is`(sourceName))
    }

    @Test
    fun `fallback to property service name if source header missing value`() {
        val result = underTest.deriveServiceName(mapOf<String, List<String?>>("Source-Name" to listOf<String>()))
        assertThat(result, `is`(appName))
    }

    @Test
    fun `fallback to property service name when no source header`() {
        val result = underTest.deriveServiceName(mapOf())
        assertThat(result, `is`(appName))
    }

    @Test
    fun `use header for target name`() {
        val result = underTest.deriveTarget(mapOf("Target-Name" to listOf(targetName)), path)
        assertThat(result, `is`(targetName))
    }

    @Test
    fun `fallback to path target header missing value`() {
        val result = underTest.deriveTarget(mapOf<String, List<String?>>("Target-Name" to listOf<String>()), path)
        assertThat(result, `is`(path))
    }

    @Test
    fun `fallback to path target if target header missing value and no path`() {
        val result = underTest.deriveTarget(mapOf<String, List<String?>>("Target-Name" to listOf<String>()), null)
        assertThat(result, `is`("UNKNOWN_TARGET"))
    }

    @Test
    fun `fallback to unknown when no target header`() {
        val result = underTest.deriveTarget(mapOf(), null)
        assertThat(result, `is`("UNKNOWN_TARGET"))
    }
}
