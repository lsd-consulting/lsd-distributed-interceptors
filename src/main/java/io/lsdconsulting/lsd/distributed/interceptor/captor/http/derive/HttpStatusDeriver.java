package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive;

import org.springframework.http.HttpStatus;

import java.util.Optional;

public class HttpStatusDeriver {
    public String derive(final int code) {
        return Optional
                .ofNullable(HttpStatus.resolve(code))
                .map(HttpStatus::toString)
                .orElse(String.format("<unresolved status:%s>", code));
    }
}
