package io.lsdconsulting.lsd.distributed.interceptor.persistence

import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import lsd.logging.log
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

class RepositoryService(
    private val threadPoolSize: Int,
    private val interceptedDocumentRepository: InterceptedDocumentRepository
) {
    private lateinit var executorService: ExecutorService

    @PostConstruct
    fun start() {
        executorService =
            ThreadPoolExecutor(threadPoolSize, threadPoolSize * 10, 0L, MILLISECONDS, LinkedBlockingQueue())
    }

    @PreDestroy
    fun stop() {
        executorService.shutdown()
    }

    fun enqueue(interceptedInteraction: InterceptedInteraction) {
        log().debug("Received interceptedInteraction: {}", interceptedInteraction)
        try {
            executorService.submit {
                log().debug("Saving interceptedInteraction: {}", interceptedInteraction)
                interceptedDocumentRepository.save(interceptedInteraction)
                log().debug("Saved interceptedInteraction: {}", interceptedInteraction)
            }
        } catch (e: RejectedExecutionException) {
            log().error("Dropping interceptedInteraction because of ${e.message}", e)
        }
    }
}
