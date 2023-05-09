package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive

import org.springframework.http.HttpRequest

fun String.toPath(): String {
    val path = this.replace(EXTRACT_PATH.toRegex(), "$1")
    return if (path == this) {
        ""
    } else path
}

fun HttpRequest.toPath(): String =
    this.uri.path + if (this.uri.query != null) "?" + this.uri.query else ""

private const val EXTRACT_PATH = "https?://.*?(/.*)"
