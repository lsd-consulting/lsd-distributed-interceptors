package io.lsdconsulting.lsd.distributed.interceptor.convert

import feign.Response
import java.io.IOException

@Throws(IOException::class)
fun Response.Body.stringify(): String {
    return String(this.asInputStream().readAllBytes())
}
