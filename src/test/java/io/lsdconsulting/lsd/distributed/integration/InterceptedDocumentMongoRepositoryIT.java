package io.lsdconsulting.lsd.distributed.integration;

import com.mongodb.client.ListIndexesIterable;
import io.lsdconsulting.lsd.distributed.captor.repository.mongo.InterceptedDocumentMongoRepository;
import io.lsdconsulting.lsd.distributed.integration.testapp.TestApplication;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RepositoryConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig(classes = {RepositoryConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@AutoConfigureWireMock(port = 0)
class InterceptedDocumentMongoRepositoryIT {

    private final TestRepository testRepository = new TestRepository();

    @Test
    public void shouldCreateCollectionWithIndexes() {
        new InterceptedDocumentMongoRepository("mongodb://" + TestRepository.MONGODB_HOST + ":" + TestRepository.MONGODB_PORT, null, null);

        final ListIndexesIterable<Document> indexes = testRepository.getCollection().listIndexes();
        final List<String> indexNames = stream(indexes.spliterator(), false)
                .map(doc -> (String) doc.get("name"))
                .collect(Collectors.toList());

        assertThat(indexNames, hasItem(startsWith("traceId")));
        assertThat(indexNames, hasItem(startsWith("createdAt")));
    }
}
