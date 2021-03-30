package com.integreety.yatspec.e2e.captor.convert;

import feign.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wiremock.org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;

import static com.integreety.yatspec.e2e.captor.convert.TypeConverter.convert;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;

class TypeConverterShould {

    @Test
    public void convertByteArrayToString() {
        final String body = RandomStringUtils.randomAlphanumeric(20);
        assertThat(convert(body.getBytes()), is(body));
    }

    @Test
    public void handleNullByteArray() {
        assertThat(convert((byte[])null), is(nullValue()));
    }

    @Test
    public void convertResponseBodyToString() throws IOException {
        final String body = RandomStringUtils.randomAlphanumeric(20);
        final Response.Body responseBody = Mockito.mock(Response.Body.class);
        given(responseBody.asInputStream()).willReturn(IOUtils.toInputStream(body));

        assertThat(convert(body.getBytes()), is(body));
    }

    @Test
    public void handleNullResponseBody() throws IOException {
        assertThat(convert((Response.Body) null), is(nullValue()));
    }
}
