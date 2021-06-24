package io.lsdconsulting.lsd.distributed.captor.repository;

import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;

import java.util.List;

/*
    We can consider providing other repositories (eg. InterceptedRelationalRepository) based on the connection string
*/
public interface InterceptedDocumentRepository {
    void save(final InterceptedInteraction interceptedInteraction);
    List<InterceptedInteraction> findByTraceIds(final String... traceId);
}