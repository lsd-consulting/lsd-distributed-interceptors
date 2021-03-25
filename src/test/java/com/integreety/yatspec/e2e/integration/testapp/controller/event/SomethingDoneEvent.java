package com.integreety.yatspec.e2e.integration.testapp.controller.event;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SomethingDoneEvent {
    String message;
}
