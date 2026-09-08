package com.bryanvalc.SpringTorch

import io.temporal.client.WorkflowClient
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.WorkerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TemporalConfiguration {
    @Bean(destroyMethod = "shutdown")
    fun workflowServiceStubs(): WorkflowServiceStubs = WorkflowServiceStubs.newServiceStubs(
        WorkflowServiceStubsOptions.newBuilder()
            .setTarget(System.getenv("TEMPORAL_ADDRESS") ?: "localhost:7233")
            .build(),
    )

    @Bean(destroyMethod = "")
    fun workflowClient(service: WorkflowServiceStubs): WorkflowClient = WorkflowClient.newInstance(service)

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    fun workerFactory(client: WorkflowClient): WorkerFactory {
        val factory = WorkerFactory.newInstance(client)
        factory.newWorker(WORKFLOW_TASK_QUEUE)
            .registerWorkflowImplementationTypes(LlmWorkflowImpl::class.java)
        return factory
    }
}
