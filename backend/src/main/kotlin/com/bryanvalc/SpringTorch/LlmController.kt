package com.bryanvalc.SpringTorch

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/llm")
class LlmController(
    private val workflowClient: WorkflowClient,
) {
    @PostMapping("/generate")
    fun generate(@RequestBody request: GenerateTextRequest): GenerateTextResponse {
        val workflow = workflowClient.newWorkflowStub(
            LlmWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(WORKFLOW_TASK_QUEUE)
                .setWorkflowId("llm-${UUID.randomUUID()}")
                .build(),
        )

        return GenerateTextResponse(workflow.generateText(request.prompt))
    }
}

data class GenerateTextRequest(
    val prompt: String,
)

data class GenerateTextResponse(
    val text: String,
)
