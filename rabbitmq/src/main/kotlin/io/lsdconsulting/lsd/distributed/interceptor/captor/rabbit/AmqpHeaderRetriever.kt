package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import org.springframework.amqp.core.Message

class AmqpHeaderRetriever(
    private val obfuscator: Obfuscator
) {
    fun retrieve(message: Message): Map<String, Collection<String>> =
        obfuscator.obfuscate(
            message.messageProperties.headers.entries.associate {
                it.key to (it.value?.let { _ -> listOf(print(it.value)) } ?: emptyList())
            }
        )
}
