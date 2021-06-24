package io.lsdconsulting.lsd.distributed.integration;

import com.mongodb.client.ListIndexesIterable;
import io.lsdconsulting.lsd.distributed.captor.repository.mongo.InterceptedDocumentMongoRepository;
import io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;

@Slf4j
class InterceptedDocumentMongoRepositoryIT {

    private final TestRepository testRepository = new TestRepository();

    @BeforeAll
    public static void setup() {
        TestRepository.setupDatabase();
    }

    @AfterAll
    public static void tearDown() {
        TestRepository.tearDownDatabase();
    }

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
