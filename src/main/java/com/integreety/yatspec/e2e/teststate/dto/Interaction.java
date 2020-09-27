package com.integreety.yatspec.e2e.teststate.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Interaction {
    String source;
    String destination;
    String path;
    String httpStatus;
    String httpMethod;
}