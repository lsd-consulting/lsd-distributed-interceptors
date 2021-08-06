package io.lsdconsulting.lsd.distributed.captor.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterceptedInteraction {
    private String traceId;
    private String body;
    private Map<String, Collection<String>> requestHeaders;
    private Map<String, Collection<String>> responseHeaders;
    private String serviceName; // the calling service or the publisher or consumer
    private String target; // the called service or the exchange name
    private String path; // the called URL or the exchange name
    private String httpStatus;
    private String httpMethod;
    private Type type;
    private String profile;
    private Long elapsedTime;
    private ZonedDateTime createdAt;
}