package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller.event;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SomethingDoneEvent {
    String message;
}
