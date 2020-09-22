package com.integreety.yatspec.e2e.captor.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterceptedCall {
    private String traceId;
    private Type type;
    private String body;
    private Map<String, Collection<String>> headers;
    private String interactionName;
}