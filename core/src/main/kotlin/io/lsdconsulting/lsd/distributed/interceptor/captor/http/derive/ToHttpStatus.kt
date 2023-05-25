package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive

fun Int.toHttpStatus() = HttpStatus.resolve(this)?.toString() ?: "<unresolved status:$this>"