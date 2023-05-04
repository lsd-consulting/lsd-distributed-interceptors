package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive

import org.springframework.http.HttpRequest

open class PathDeriver {
    fun derivePathFrom(url: String): String {
        val path = url.replace(EXTRACT_PATH.toRegex(), "$1")
        return if (path == url) {
            ""
        } else path
    }

    fun derivePathFrom(request: HttpRequest): String =
        request.uri.path + if (request.uri.query != null) "?" + request.uri.query else ""

    companion object {
        private const val EXTRACT_PATH = "https?://.*?(/.*)"
    }
}
