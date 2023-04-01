package io.lsdconsulting.lsd.distributed.interceptor.persistance

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction

data class Event(
    var interceptedInteraction: InterceptedInteraction? = null
)
