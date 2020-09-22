package com.integreety.yatspec.e2e.captor.repository;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;

import java.util.List;

/*
    We can consider providing other repositories (eg. InterceptedRelationalRepository) based on the connection string
*/
public interface InterceptedDocumentRepository {
    void save(final InterceptedCall interceptedCall);
    List<InterceptedCall> findByTraceId(final String traceId);
}