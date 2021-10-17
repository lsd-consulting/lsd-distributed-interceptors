package io.lsdconsulting.lsd.distributed.interceptor.integration.extension;

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MongodbExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    @Override
    public void beforeAll(ExtensionContext context) {
    }

    @Override
    public void close() {
        TestRepository.tearDownDatabase();
    }
}