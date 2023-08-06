package io.lsdconsulting.lsd.distributed.interceptor.captor.common

import lsd.format.config.log
import lsd.format.json.objectMapper
import java.io.BufferedReader
import java.io.InputStream

fun print(obj: Any?): String =
    obj?.let {
        try {
            when (obj) {
                is InputStream -> {
                    obj.bufferedReader().use(BufferedReader::readText).trim()
                }

                is ByteArray -> {
                    if (obj.isEmpty()) "" else String(obj).trim()
                }

                is String -> {
                    if (obj.isEmpty()) "" else obj.trim()
                }

                else -> objectMapper.writeValueAsString(obj)
            }
        } catch (e: Exception) {
            log().error("Problem serialising intercepted object for LSD: {}", e.message)
            ""
        }
    } ?: ""
