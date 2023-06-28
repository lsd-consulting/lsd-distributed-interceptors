package io.lsdconsulting.lsd.distributed.interceptor.captor

import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpResponse

class HttpHeaderRetriever(
    private val obfuscator: Obfuscator
) {
    fun retrieve(request: HttpRequest): Map<String, Collection<String>> =
        obfuscator.obfuscate(request.headers.entries.associate { it.key to it.value })

    fun retrieve(response: ClientHttpResponse): Map<String, Collection<String>> =
        obfuscator.obfuscate(response.headers.entries.associate { it.key to it.value })
}
