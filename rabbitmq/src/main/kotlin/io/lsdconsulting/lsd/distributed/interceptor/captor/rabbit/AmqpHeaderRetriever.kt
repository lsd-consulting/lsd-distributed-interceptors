package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import lsd.format.printFlat
import org.springframework.amqp.core.Message

class AmqpHeaderRetriever(
    private val obfuscator: Obfuscator
) {
    fun retrieve(message: Message): Map<String, Collection<String>> =
        obfuscator.obfuscate(
            message.messageProperties.headers.entries.associate {
                it.key to (it.value?.let { _ -> listOf(printFlat(it.value)) } ?: emptyList())
            }
        )
}
