package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import lsd.format.json.objectMapper
import org.apache.avro.AvroRuntimeException

fun serialiseWithAvro(obj: Any): String = try {
    objectMapper.writeValueAsString(obj)
} catch (e: InvalidDefinitionException) {
    obj.toString()
} catch (e: JsonMappingException) {
    if (e.cause is AvroRuntimeException) {
        obj.toString()
    } else {
        throw e
    }
}
