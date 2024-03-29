package io.lsdconsulting.lsd.distributed.interceptor.captor.common

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import lsd.format.json.objectMapper
import lsd.logging.log
import java.io.BufferedReader
import java.io.InputStream

@Suppress("LoggingStringTemplateAsArgument")
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

                else -> {
                    try {
                        return objectMapper.writeValueAsString(obj)
                    } catch (e: InvalidDefinitionException) {
                        return obj.toString()
                    }
                }
            }
        } catch (e: StackOverflowError) {
            log().error("Problem serialising intercepted object for LSD - probably self referencing object with a broken toString() implementation")
            ""
        } catch (e: Throwable) {
            log().error("Problem serialising intercepted object for LSD: {}", e.message)
            if (log().isTraceEnabled) {
                log().trace("Problem serialising intercepted object for LSD:$obj", e)
            }
            ""
        }
    } ?: ""
