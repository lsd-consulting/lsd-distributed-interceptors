package io.lsdconsulting.lsd.distributed.interceptor.convert

import feign.Response
import org.apache.commons.io.IOUtils
import org.apache.commons.io.IOUtils.*
import java.io.IOException

@Throws(IOException::class)
fun Response.Body.stringify() = String(toByteArray(this.asInputStream()))
