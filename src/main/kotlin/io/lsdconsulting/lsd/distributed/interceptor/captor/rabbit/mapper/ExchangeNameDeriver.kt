package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper

import org.apache.commons.lang3.StringUtils
import org.springframework.amqp.core.MessageProperties

class ExchangeNameDeriver {
    fun derive(messageProperties: MessageProperties, alternativeExchangeName: String?): String =
        getDefaultExchangeName(alternativeExchangeName).let { defaultExchangeName ->
            if (!StringUtils.isBlank(messageProperties.getHeader(TARGET_NAME_KEY))) {
                messageProperties.getHeader(TARGET_NAME_KEY)
            } else if (!StringUtils.isBlank(messageProperties.getHeader(TYPE_ID_HEADER))) {
                deriveFromTypeIdHeader(messageProperties.getHeader(TYPE_ID_HEADER)) ?: defaultExchangeName
            } else defaultExchangeName
        }

    private fun getDefaultExchangeName(alternativeExchangeName: String?): String =
         if (!alternativeExchangeName.isNullOrBlank()) alternativeExchangeName else UNKNOWN_EVENT

    private fun deriveFromTypeIdHeader(typeIdHeader: String): String =
        typeIdHeader.split("\\.".toRegex()).reduce { _: String?, second: String -> second }

    companion object {
        private const val TYPE_ID_HEADER = "__TypeId__"
        private const val TARGET_NAME_KEY = "Target-Name"
        private const val UNKNOWN_EVENT = "UNKNOWN_EVENT"
    }
}
