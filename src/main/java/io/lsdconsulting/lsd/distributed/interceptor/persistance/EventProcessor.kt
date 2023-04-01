package io.lsdconsulting.lsd.distributed.interceptor.persistance

import com.lmax.disruptor.EventHandler
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository

class EventProcessor(
    private val interceptedDocumentRepository: InterceptedDocumentRepository
) : EventHandler<Event> {
    override fun onEvent(event: Event, sequence: Long, endOfBatch: Boolean) {
        interceptedDocumentRepository.save(event.interceptedInteraction)
    }
}
