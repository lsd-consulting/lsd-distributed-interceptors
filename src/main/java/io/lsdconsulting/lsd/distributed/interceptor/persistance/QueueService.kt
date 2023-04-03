package io.lsdconsulting.lsd.distributed.interceptor.persistance

import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.dsl.Disruptor
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

class QueueService(
    private val bufferSize: Int,
    private val eventProcessor: EventProcessor
) {
    private lateinit var disruptor: Disruptor<Event>
    private lateinit var ringBuffer: RingBuffer<Event>

    @PostConstruct
    fun start() {
        val eventFactory: EventFactory<Event> = EventFactory { Event() }

        disruptor = Disruptor(eventFactory, bufferSize, Executors.newSingleThreadExecutor())
        disruptor.handleEventsWith(eventProcessor)

        ringBuffer = disruptor.start()
    }

    @PreDestroy
    fun stop() {
        disruptor.shutdown()
    }

    fun enqueue(interceptedInteraction: InterceptedInteraction) {
        ringBuffer.publishEvent { event, _ -> event.interceptedInteraction = interceptedInteraction }
    }
}
