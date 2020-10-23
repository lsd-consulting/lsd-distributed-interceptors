package com.integreety.yatspec.e2e.integration;

import com.integreety.yatspec.e2e.captor.repository.mongo.InterceptedDocumentMongoRepository;
import com.integreety.yatspec.e2e.integration.testapp.repository.TestRepository;
import com.mongodb.client.ListIndexesIterable;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.integreety.yatspec.e2e.integration.testapp.repository.TestRepository.*;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;

@Slf4j
class InterceptedDocumentMongoRepositoryIT {

    private final TestRepository testRepository = new TestRepository();

    @BeforeAll
    public static void setup() {
        setupDatabase();
    }

    @AfterAll
    public static void tearDown() {
        tearDownDatabase();
    }

    @Test
    public void shouldCreateCollectionWithIndexes() {
        new InterceptedDocumentMongoRepository("mongodb://" + MONGODB_HOST + ":" + MONGODB_PORT, null, null);

        final ListIndexesIterable<Document> indexes = testRepository.getCollection().listIndexes();
        final List<String> indexNames = stream(indexes.spliterator(), false)
                .map(doc -> (String) doc.get("name"))
                .collect(Collectors.toList());

        assertThat(indexNames, hasItem(startsWith("traceId")));
        assertThat(indexNames, hasItem(startsWith("createdAt")));
    }
}
