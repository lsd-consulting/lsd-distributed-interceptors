package io.lsdconsulting.lsd.distributed.interceptor.captor.common

class PropertyServiceNameDeriver(appName: String) {
    val serviceName: String = appName
        .replace(DEFAULT_SERVICE_NAME_SUFFIX_TO_REMOVE.toRegex(), "")

    companion object {
        // TODO This should be configurable
        private const val DEFAULT_SERVICE_NAME_SUFFIX_TO_REMOVE = " Service"
    }
}
