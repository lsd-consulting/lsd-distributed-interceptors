package io.lsdconsulting.lsd.distributed.interceptor.integration.listener;

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

@Slf4j
public class MongoDbTestListener implements TestExecutionListener {

    public void testPlanExecutionStarted(TestPlan testPlan) {
        log.info("MongoDbTestListener.testPlanExecutionStarted");
        TestRepository.setupDatabase();
    }

    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.info("MongoDbTestListener.testPlanExecutionFinished");
        TestRepository.tearDownDatabase();
    }
}
