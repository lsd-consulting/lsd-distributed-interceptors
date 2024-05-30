package io.lsdconsulting.lsd.distributed.interceptor.captor.common

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import lsd.format.json.objectMapper
import org.approvaltests.Approvals.verify
import org.approvaltests.Approvals.verifyAll
import org.approvaltests.core.Options
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Stream


internal class PrinterShould {
    private val jsonOptions = Options().forFile().withExtension(".json")
    private val xmlOptions = Options().forFile().withExtension(".xml")
    private val txtOptions = Options()

    @AfterEach
    fun setup() {
        unmockkAll()
    }

    @Test
    fun `flatten json`() {
        verify(print(readDocument("/source/pretty.json")), jsonOptions)
    }

    @Test
    fun `flatten json from bytes`() {
        verify(print(readDocument("/source/pretty.json").toByteArray(StandardCharsets.UTF_8)), jsonOptions)
    }

    @Test
    fun `flatten json from object`() {
        verify(print(ExampleObject(2)), jsonOptions)
    }

    @Test
    fun `handle empty string`() {
        verify(print(""), jsonOptions)
    }

    @Test
    fun `handle blank string`() {
        verify(print(" "), jsonOptions)
    }

    @Test
    fun `handle null value`() {
        verify(print(null), jsonOptions)
    }

    @Test
    fun `handle empty json byte array`() {
        verify(print("".toByteArray(StandardCharsets.UTF_8)), jsonOptions)
    }

    @Test
    fun `handle blank json byte array`() {
        verify(print(" ".toByteArray(StandardCharsets.UTF_8)), jsonOptions)
    }

    @Test
    fun `handle array of empty objects`() {
        verify(print(arrayOf(Any())), jsonOptions)
    }

    @Test
    fun `handle empty array of objects`() {
        verify(print(arrayOf<Any>()), jsonOptions)
    }

    @Test
    fun `handle array of objects`() {
        verify(print(arrayOf(ExampleObject(1), ExampleObject(2))), jsonOptions)
    }

    @Test
    fun `format top level json array`() {
        verify(print(readDocument("/source/prettyTopLevelArray.json")), jsonOptions)
    }

    @Test
    fun `format xml`() {
        verify(print(readDocument("/source/pretty.xml")), xmlOptions)
    }

    @Test
    fun `format xml with multiline header`() {
        verify(print(readDocument("/source/multilineHeader.xml")), xmlOptions)
    }

    @Test
    fun `return original if neither json nor xml`() {
        verify(print(readDocument("/source/source.txt")))
    }

    @Test
    fun `convert byte array field to string`() {
        val objects = byteArrayExamples()
            .map { ExampleObjectWithBytes(it) }
            .map { print(it) }
            .toArray()
        verifyAll("an object with bytes[] field.", objects)
    }

    @Test
    fun `handle serialisation failure`() {
        mockkStatic("lsd.format.json.ObjectMapperCreatorKt")

        every { objectMapper } throws Exception("Blah")

        verify(print(ExampleObject()), jsonOptions)
    }

    @Test
    fun `handle custom exceptions`() {
        mockkStatic("lsd.format.json.ObjectMapperCreatorKt")

        val ex = JsonMappingException(null, "", SomeException())
        every { objectMapper } throws ex

        verify(print(ExampleObject()) { obj ->
            try {
                objectMapper.writeValueAsString(obj)
            } catch (e: InvalidDefinitionException) {
                obj.toString()
            } catch (e: JsonMappingException) {
                if (e.cause is SomeException) {
                    obj.toString()
                } else {
                    throw e
                }
            }
        }, txtOptions)
    }

    class SomeException: Exception("blah!")

    @Test
    fun `handle self referenced objects`() {
        val selfReference = SelfReference()
        selfReference.value = selfReference
        verify(print(selfReference), txtOptions)
    }

    @Test
    fun `handle self referenced objects with a broken toString implementation`() {
        val selfReference = SelfReferenceWithBrokenToString()
        selfReference.value = selfReference
        verify(print(selfReference), jsonOptions)
    }

    private fun byteArrayExamples(): Stream<ByteArray?> {
        return Stream.of(
            "".toByteArray(),
            " ".toByteArray(),
            "some regular text".toByteArray(),
            "{looks like json}".toByteArray(),
            "{\"name\":\"Bond\", \"age\":164, \"hungry\":true}".toByteArray(),
            "[{\"name\":\"Bond\", \"age\":164, \"hungry\":true}]".toByteArray(),
            "<xml><x>hello</x><y>goodbye</y></xml>".toByteArray(),
            "<looks like xml>".toByteArray(),
            null, ByteArray(0), byteArrayOf(1, 2, 3),
        )
    }

    private fun readDocument(fileName: String): String {
        return Files.readString(
            Paths.get(Objects.requireNonNull(javaClass.getResource(fileName)).toURI()),
            StandardCharsets.UTF_8
        )
    }

    private data class ExampleObject(
        var value: Int = 0
    )

    private data class ExampleObjectWithBytes(
        val value: ByteArray?
    )

    private data class SelfReference(
        var value: SelfReference? = null
    )
    {
        override fun toString(): String {
            return "SelfReference"
        }
    }

    private data class SelfReferenceWithBrokenToString(
        var value: SelfReferenceWithBrokenToString? = null
    )
}
