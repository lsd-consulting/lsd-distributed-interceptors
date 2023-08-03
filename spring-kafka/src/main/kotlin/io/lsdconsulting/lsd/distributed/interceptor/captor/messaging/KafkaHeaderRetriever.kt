package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import org.apache.kafka.common.header.Headers

class KafkaHeaderRetriever(
    private val obfuscator: Obfuscator
) {
    fun retrieve(headers: Headers): Map<String, Collection<String>> =
        obfuscator.obfuscate(headers.associate {
            it.key() to (it.value()?.let { _ -> listOf(String(it.value())) } ?: emptyList())
        })
}
