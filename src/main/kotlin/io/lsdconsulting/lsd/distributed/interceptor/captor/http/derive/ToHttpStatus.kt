package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive

import org.springframework.http.HttpStatus

    fun Int.toHttpStatus() = HttpStatus.resolve(this)?.toString() ?: String.format("<unresolved status:%s>", this)