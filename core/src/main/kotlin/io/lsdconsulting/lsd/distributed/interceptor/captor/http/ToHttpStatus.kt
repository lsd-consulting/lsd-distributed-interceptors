package io.lsdconsulting.lsd.distributed.interceptor.captor.http

fun Int.toHttpStatus() = HttpStatus.resolve(this)?.toString() ?: "<unresolved status:$this>"