package io.lsdconsulting.lsd.distributed.interceptor.captor.convert

import feign.Response
import org.apache.commons.io.IOUtils
import java.io.IOException

object TypeConverter {
    fun convert(body: ByteArray?): String? =
        if (body != null) String(body) else null

    @Throws(IOException::class)
    fun convert(body: Response.Body?): String? =
        if (body != null) String(IOUtils.toByteArray(body.asInputStream())) else null
}
