package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import lsd.format.json.objectMapper
import lsd.logging.log
import org.apache.avro.AvroRuntimeException
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

                else -> {
                    try {
                        return objectMapper.writeValueAsString(obj)
                    } catch (e: InvalidDefinitionException) {
                        return obj.toString()
                    } catch (e: JsonMappingException) {
                        if (e.cause is AvroRuntimeException) {
                            return obj.toString()
                        }
                        throw e
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
