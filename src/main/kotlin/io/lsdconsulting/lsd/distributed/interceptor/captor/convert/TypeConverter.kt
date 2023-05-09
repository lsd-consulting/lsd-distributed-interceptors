package io.lsdconsulting.lsd.distributed.interceptor.captor.convert

import feign.Response
import org.apache.commons.io.IOUtils
import java.io.IOException

fun ByteArray.convert() = String(this)

@Throws(IOException::class)
fun Response.Body.convert() = String(IOUtils.toByteArray(this.asInputStream()))
