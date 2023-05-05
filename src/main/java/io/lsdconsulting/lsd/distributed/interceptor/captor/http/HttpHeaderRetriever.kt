package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import feign.Request
import feign.Response
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

    fun retrieve(request: Request): Map<String, Collection<String>> =
        obfuscator.obfuscate(request.headers())

    fun retrieve(response: Response): Map<String, Collection<String>> =
        obfuscator.obfuscate(response.headers())
}
