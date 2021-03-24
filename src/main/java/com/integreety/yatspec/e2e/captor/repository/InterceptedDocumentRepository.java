package com.integreety.yatspec.e2e.captor.repository;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;

import java.util.List;

/*
    We can consider providing other repositories (eg. InterceptedRelationalRepository) based on the connection string
*/
public interface InterceptedDocumentRepository {
    void save(final InterceptedInteraction interceptedInteraction);
    List<InterceptedInteraction> findByTraceId(final String... traceId);
}