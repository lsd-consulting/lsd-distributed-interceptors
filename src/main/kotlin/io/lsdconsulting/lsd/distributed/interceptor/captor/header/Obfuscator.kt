package io.lsdconsulting.lsd.distributed.interceptor.captor.header

class Obfuscator(headers: String?) {
    private val sensitiveHeaders =  headers?.split(DELIMINATOR)?.map { it.trim() } ?: emptyList()

    fun obfuscate(headers: Map<String, Collection<String>>): Map<String, Collection<String>> {
        val obfuscatedHeaders = headers
            .filter { sensitiveHeaders.contains(it.key) }
            .map { it.key to listOf("<obfuscated>") }

        val compiledHeaders = headers.toMutableMap()
        compiledHeaders.putAll(obfuscatedHeaders)
        return compiledHeaders
    }

    companion object {
        const val DELIMINATOR = ","
    }
}
