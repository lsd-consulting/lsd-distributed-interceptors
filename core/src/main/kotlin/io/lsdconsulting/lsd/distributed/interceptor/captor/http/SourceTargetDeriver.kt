package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import javax.annotation.PostConstruct

class SourceTargetDeriver(
    private val propertyServiceNameDeriver: PropertyServiceNameDeriver,
) {

    private lateinit var serviceName: String

    @PostConstruct
    fun initialise() {
        serviceName = propertyServiceNameDeriver.serviceName
    }

    fun deriveServiceName(headers: Map<String, Collection<String?>>): String =
        if (headerExists(headers, SOURCE_NAME_KEY)) findHeader(
            headers,
            SOURCE_NAME_KEY
        ) ?: serviceName else serviceName

    fun deriveTarget(headers: Map<String, Collection<String?>>, path: String?): String =
        if (headerExists(headers, TARGET_NAME_KEY)) findHeader(headers, TARGET_NAME_KEY) ?: path
        ?: UNKNOWN_TARGET
        else UNKNOWN_TARGET

    private fun findHeader(headers: Map<String, Collection<String?>>, targetNameKey: String): String? =
        headers[targetNameKey]?.firstOrNull { !it.isNullOrBlank() }

    private fun headerExists(headers: Map<String, Collection<String?>>, targetNameKey: String): Boolean =
        headers.isNotEmpty() && headers.containsKey(targetNameKey)

    companion object {
        const val SOURCE_NAME_KEY = "Source-Name"
        const val TARGET_NAME_KEY = "Target-Name"
        const val UNKNOWN_TARGET = "UNKNOWN_TARGET"
    }
}
