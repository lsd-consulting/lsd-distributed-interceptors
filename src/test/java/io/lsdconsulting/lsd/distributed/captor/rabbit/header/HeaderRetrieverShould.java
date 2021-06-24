package io.lsdconsulting.lsd.distributed.captor.rabbit.header;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class HeaderRetrieverShould {

    private final HeaderRetriever headerRetriever = new HeaderRetriever();

    @Test
    public void retrieveSingleHeader() {
        final MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("name", "value");

        final Map<String, Collection<String>> headers = headerRetriever.retrieve(messageProperties);

        assertThat(headers.keySet(), hasSize(1));
        assertThat(headers.get("name"), equalTo(singletonList("value")));
    }

    @Test
    public void retrieveMultipleHeaders() {
        final MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("name1", "value1");
        messageProperties.setHeader("name2", "value2");

        final Map<String, Collection<String>> headers = headerRetriever.retrieve(messageProperties);

        assertThat(headers.keySet(), hasSize(2));
        assertThat(headers.get("name1"), equalTo(singletonList("value1")));
        assertThat(headers.get("name2"), equalTo(singletonList("value2")));
    }

    @Test
    public void handleHeadersWithNoValues() {
        final MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("name", null);

        final Map<String, Collection<String>> headers = headerRetriever.retrieve(messageProperties);

        assertThat(headers.keySet(), hasSize(1));
        assertThat(headers.get("name"), is(List.of()));
    }

    @Test
    public void handleNoHeaders() {
        final Map<String, Collection<String>> headers = headerRetriever.retrieve(new MessageProperties());

        assertThat(headers.keySet(), hasSize(0));
    }
}