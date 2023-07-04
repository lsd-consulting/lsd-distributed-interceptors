package io.lsdconsulting.lsd.distributed.interceptor.captor.http

fun String.toPath(): String {
    val path = this.replace(EXTRACT_PATH.toRegex(), "$1")
    return if (path == this) {
        ""
    } else path
}

private const val EXTRACT_PATH = "https?://.*?(/.*)"
