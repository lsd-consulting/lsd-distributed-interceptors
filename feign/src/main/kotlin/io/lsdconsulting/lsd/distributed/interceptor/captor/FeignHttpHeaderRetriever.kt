package io.lsdconsulting.lsd.distributed.interceptor.captor

import feign.Request
import feign.Response
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator

class FeignHttpHeaderRetriever(
    private val obfuscator: Obfuscator
) {
    fun retrieve(request: Request): Map<String, Collection<String>> =
        obfuscator.obfuscate(request.headers())

    fun retrieve(response: Response): Map<String, Collection<String>> =
        obfuscator.obfuscate(response.headers())
}
