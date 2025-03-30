package io.lsdconsulting.lsd.distributed.interceptor.captor.common

import org.apache.commons.lang3.RandomStringUtils.secure
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasEntry
import org.junit.jupiter.api.Test

internal class ObfuscatorShould {

    private val underTest = Obfuscator("Authorization,JWT")

    @Test
    fun `obfuscate authorization header`() {
        val headers = mapOf<String, Collection<String>>("Authorization" to listOf(secure().nextAlphanumeric(30)))
        val result = underTest.obfuscate(headers)
        assertThat(result, hasEntry("Authorization", listOf("<obfuscated>")))
    }

    @Test
    fun `obfuscate jwtheader`() {
        val headers = mapOf<String, Collection<String>>("JWT" to listOf(secure().nextAlphanumeric(30)))
        val result = underTest.obfuscate(headers)
        assertThat(result, hasEntry("JWT", listOf("<obfuscated>")))
    }

    @Test
    fun `keep other headers unchanged`() {
        val headerName = secure().nextAlphanumeric(30)
        val headerValue = secure().nextAlphanumeric(30)
        val headers = mapOf<String, Collection<String>>(headerName to listOf(headerValue))
        val result = underTest.obfuscate(headers)
        assertThat(result, hasEntry(headerName, listOf(headerValue)))
    }

    @Test
    fun `accept no headers`() {
        val underTest = Obfuscator(null)
        val headerName = secure().nextAlphanumeric(30)
        val headerValue = secure().nextAlphanumeric(30)
        val headers = mapOf<String, Collection<String>>(headerName to listOf(headerValue))
        val result = underTest.obfuscate(headers)
        assertThat(result, hasEntry(headerName, listOf(headerValue)))
    }
}
