package io.lsdconsulting.lsd.distributed.interceptor.captor.common

class PropertyServiceNameDeriver(appName: String) {
    val serviceName: String

    init {
        serviceName = appName
            .replace(DEFAULT_SERVICE_NAME_SUFFIX_TO_REMOVE.toRegex(), "")
            .replace(" ".toRegex(), "")
    }

    companion object {
        // TODO This should be configurable
        private const val DEFAULT_SERVICE_NAME_SUFFIX_TO_REMOVE = " Service"
    }
}
