package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive

import org.springframework.http.HttpStatus

class HttpStatusDeriver {
    fun derive(code: Int) = HttpStatus.resolve(code)?.toString() ?: String.format("<unresolved status:%s>", code)
}