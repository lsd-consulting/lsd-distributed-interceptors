package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller

import lsd.logging.log
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
