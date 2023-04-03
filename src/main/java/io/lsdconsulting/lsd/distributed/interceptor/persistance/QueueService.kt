package io.lsdconsulting.lsd.distributed.interceptor.persistance

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

class QueueService(
    private val threadPoolSize: Int,
    private val interceptedDocumentRepository: InterceptedDocumentRepository
) {
    private lateinit var executorService: ExecutorService

    @PostConstruct
    fun start() {
        executorService = Executors.newFixedThreadPool(threadPoolSize)
    }

    @PreDestroy
    fun stop() {
        executorService.shutdown()
    }

    fun enqueue(interceptedInteraction: InterceptedInteraction) {
        executorService.submit { interceptedDocumentRepository.save(interceptedInteraction) }
    }
}
