package com.integreety.yatspec.e2e.diagram;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;

import static com.mongodb.client.model.Filters.eq;

public class TestStateCollector {

    private static final String DATABASE_NAME = "goms";
    private static final String COLLECTION_NAME = "interceptedInteraction";

    private final TestState testState;

    private final MongoClient mongoClient;

    public TestStateCollector(final String dbConnectionString, final TestState testState) {
        this.testState = testState;
        final ConnectionString connString = new ConnectionString(dbConnectionString);
        mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(connString)
//            .credential(credential)
                .retryWrites(true)
                .build());
    }

    public void createYatspecDiagram(final String traceId) {
        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        final MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        final FindIterable<Document> documents = collection.find(eq("traceId", traceId));
        for (final Document document : documents) {
            final String interactionName = document.get("interactionName", String.class);
            testState.log(interactionName, getPrettyString(document));
        }
    }

    private String getPrettyString(final Document document) {
        String prettyJsonString;
        try {
            prettyJsonString = prettifyJson(document);
        } catch(final Exception e) {
            prettyJsonString = prettifyXml(document);
        }
        return prettyJsonString;
    }

    private String prettifyJson(final Document document) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String prettyJsonString;
        final JsonElement je = JsonParser.parseString(document.get("body", String.class));
        prettyJsonString = gson.toJson(je);
        return prettyJsonString;
    }

    private String prettifyXml(final Document document) {
        final String prettyJsonString;
        final StringWriter sw = new StringWriter();
        try {
            final org.dom4j.io.OutputFormat format = org.dom4j.io.OutputFormat.createPrettyPrint();
            final org.dom4j.Document xml = DocumentHelper.parseText(document.get("body", String.class));
            final XMLWriter writer = new XMLWriter(sw, format);
            writer.write(xml);
        } catch (final IOException | DocumentException ioException) {
            ioException.printStackTrace();
        }

        prettyJsonString = sw.toString();
        return prettyJsonString;
    }
}
