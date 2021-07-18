package io.lsdconsulting.lsd.distributed.captor.convert;

import feign.Response;
import lombok.NoArgsConstructor;

import java.io.IOException;

import static feign.Util.toByteArray;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class TypeConverter {

    public static String convert(final byte[] body) {
        return body != null ? new String(body) : null;
    }

    public static String convert(final Response.Body body) throws IOException {
        return body != null ? new String(toByteArray(body.asInputStream())) : null;
    }
}
