package io.lsdconsulting.lsd.distributed.interceptor.captor.convert;

import feign.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;

class TypeConverterShould {

    @Test
    public void convertByteArrayToString() {
        final String body = randomAlphanumeric(20);
        assertThat(TypeConverter.convert(body.getBytes()), is(body));
    }

    @Test
    public void handleNullByteArray() {
        assertThat(TypeConverter.convert((byte[])null), is(nullValue()));
    }

    @Test
    public void convertResponseBodyToString() throws IOException {
        final String body = randomAlphanumeric(20);
        final Response.Body responseBody = Mockito.mock(Response.Body.class);
        given(responseBody.asInputStream()).willReturn(IOUtils.toInputStream(body));

        assertThat(TypeConverter.convert(body.getBytes()), is(body));
    }

    @Test
    public void handleNullResponseBody() throws IOException {
        assertThat(TypeConverter.convert((Response.Body) null), is(nullValue()));
    }
}
