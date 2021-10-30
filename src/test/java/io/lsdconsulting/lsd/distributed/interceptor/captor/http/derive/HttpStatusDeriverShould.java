package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

class HttpStatusDeriverShould {

    @Test
    void handleUnknownStatusCode() {
        String result = new HttpStatusDeriver().derive(RandomUtils.nextInt(1000, 10000));

        assertThat(result, startsWith("<unresolved status:"));
    }

    @Test
    void handleKnownStatusCode() {
        String result = new HttpStatusDeriver().derive(200);

        assertThat(result, is("200 OK"));
    }
}
