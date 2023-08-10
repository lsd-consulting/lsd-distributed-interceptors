package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import io.lsdconsulting.lsd.distributed.interceptor.config.MessagingConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Import
import org.springframework.integration.config.GlobalChannelInterceptor
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Service

private const val SOURCE_NAME = "Source-Name"
private const val TARGET_NAME = "Target-Name"

@Service
@Import(MessagingConfig::class)
@GlobalChannelInterceptor(patterns = ["*inputOutputHandlerFunction-out-*"], order = -1)
class LsdConfig(@Value("\${info.app.name}") val appName: String) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val headers: MutableMap<String, Any> = message.headers.entries
            .filter { !it.key.equals(SOURCE_NAME) && !it.key.equals(TARGET_NAME) }
            .associate { it.key to it.value }.toMutableMap()
        headers[SOURCE_NAME] = appName.replace(" ", "")
        headers[TARGET_NAME] = "output.topic"
        return GenericMessage(message.payload, headers.toMap())
    }
}
