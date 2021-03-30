package com.integreety.yatspec.e2e.captor.convert;

import feign.Response;

import java.io.IOException;

import static feign.Util.toByteArray;

public class TypeConverter {

    public static String convert(final byte[] body) {
        return body != null ? new String(body) : null;
    }

    public static String convert(final Response.Body body) throws IOException {
        return body != null ? new String(toByteArray(body.asInputStream())) : null;
    }
}