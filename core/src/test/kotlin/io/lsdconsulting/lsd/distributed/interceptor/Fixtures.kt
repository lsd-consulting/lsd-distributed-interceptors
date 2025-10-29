package io.lsdconsulting.lsd.distributed.interceptor
import org.apache.commons.lang3.RandomStringUtils.insecure
import org.apache.commons.lang3.RandomUtils

fun randomAlphabetic(length: Int): String = insecure().nextAlphabetic(length)
fun randomAlphanumeric(length: Int): String = insecure().nextAlphanumeric(length)
fun randomInt(start: Int, end: Int): Int = RandomUtils.insecure().randomInt(start, end)