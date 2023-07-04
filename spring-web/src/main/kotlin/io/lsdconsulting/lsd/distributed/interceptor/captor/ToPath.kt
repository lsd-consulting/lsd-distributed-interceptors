package io.lsdconsulting.lsd.distributed.interceptor.captor

import org.springframework.http.HttpRequest

fun HttpRequest.toPath(): String =
    this.uri.path + if (this.uri.query != null) "?" + this.uri.query else ""
