package com.integreety.yatspec.e2e.diagram;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class TestStateCollector {

    private final TestState testState;

    private final InterceptedDocumentRepository interceptedDocumentRepository;

    public TestStateCollector(final TestState testState, final InterceptedDocumentRepository interceptedDocumentRepository) {
        this.testState = testState;
        this.interceptedDocumentRepository = interceptedDocumentRepository;
    }

    public void logStatesFromDatabase(final String traceId) {
        final List<InterceptedCall> data = interceptedDocumentRepository.findByTraceId(traceId);
        for (final InterceptedCall interceptedCall : data) {
            final String interactionName = interceptedCall.getInteractionName();
            testState.log(interactionName, getPrettyString(interceptedCall));
        }
    }

    private String getPrettyString(final InterceptedCall document) {
        String prettyJsonString;
        try {
            prettyJsonString = prettifyJson(document);
        } catch(final Exception e) {
            prettyJsonString = prettifyXml(document);
        }
        return prettyJsonString;
    }

    private String prettifyJson(final InterceptedCall document) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String prettyJsonString;
        final JsonElement je = JsonParser.parseString(document.getBody());
        prettyJsonString = gson.toJson(je);
        return prettyJsonString;
    }

    private String prettifyXml(final InterceptedCall document) {
        final String prettyJsonString;
        final StringWriter sw = new StringWriter();
        try {
            final org.dom4j.io.OutputFormat format = org.dom4j.io.OutputFormat.createPrettyPrint();
            final org.dom4j.Document xml = DocumentHelper.parseText(document.getBody());
            final XMLWriter writer = new XMLWriter(sw, format);
            writer.write(xml);
        } catch (final IOException | DocumentException ioException) {
            ioException.printStackTrace();
        }

        prettyJsonString = sw.toString();
        return prettyJsonString;
    }
}