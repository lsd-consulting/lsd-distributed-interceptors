package io.lsdconsulting.lsd.distributed.interceptor.captor.convert

import feign.Response
import org.apache.commons.io.IOUtils
import java.io.IOException

fun ByteArray.stringify() = String(this)

@Throws(IOException::class)
fun Response.Body.stringify() = String(IOUtils.toByteArray(this.asInputStream()))
