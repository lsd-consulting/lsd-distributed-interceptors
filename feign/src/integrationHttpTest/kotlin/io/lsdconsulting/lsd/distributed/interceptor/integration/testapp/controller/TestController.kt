package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller

import io.lsdconsulting.lsd.distributed.interceptor.config.log
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/get-api")
    fun get(@RequestParam message: String): ResponseEntity<String> {
        log().info("Received message:{}", message)
        return ResponseEntity.ok("Response:$message")
    }

    @PostMapping("/post-api")
    fun post(@RequestBody message: String): ResponseEntity<String> {
        log().info("Received message:{}", message)
        return ResponseEntity.ok("Response:$message")
    }
}
