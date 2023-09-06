package io.lsdconsulting.lsd.distributed.interceptor.integration.listener

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository
import lsd.logging.log
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan

class MongoDbTestListener : TestExecutionListener {
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        log().info("MongoDbTestListener.testPlanExecutionStarted")
        TestRepository.setupDatabase()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        log().info("MongoDbTestListener.testPlanExecutionFinished")
        TestRepository.tearDownDatabase()
    }
}
