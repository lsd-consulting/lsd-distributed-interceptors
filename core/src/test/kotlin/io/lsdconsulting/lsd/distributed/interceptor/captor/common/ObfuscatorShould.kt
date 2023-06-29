package io.lsdconsulting.lsd.distributed.interceptor.captor.common

import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasEntry
import org.junit.jupiter.api.Test

internal class ObfuscatorShould {

    private val underTest = Obfuscator("Authorization,JWT")

    @Test
    fun `obfuscate authorization header`() {
        val headers = mapOf<String, Collection<String>>("Authorization" to listOf(randomAlphanumeric(30)))
        val result = underTest.obfuscate(headers)
        assertThat(result, hasEntry("Authorization", listOf("<obfuscated>")))
    }

    @Test
    fun `obfuscate jwtheader`() {
        val headers = mapOf<String, Collection<String>>("JWT" to listOf(randomAlphanumeric(30)))
        val result = underTest.obfuscate(headers)
        assertThat(result, hasEntry("JWT", listOf("<obfuscated>")))
    }

    @Test
    fun `keep other headers unchanged`() {
        val headerName = randomAlphanumeric(30)
        val headerValue = randomAlphanumeric(30)
        val headers = mapOf<String, Collection<String>>(headerName to listOf(headerValue))
        val result = underTest.obfuscate(headers)
        assertThat(result, hasEntry(headerName, listOf(headerValue)))
    }
}
