package com.integreety.yatspec.e2e.integration.testapp.api.response;

import lombok.*;

import java.time.ZonedDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class TestResponse {
    private Long id;
    private String message;
    private Long number;
    private ZonedDateTime created;
}
