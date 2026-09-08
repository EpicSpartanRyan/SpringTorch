package com.bryanvalc.SpringTorch

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import java.time.Duration

const val WORKFLOW_TASK_QUEUE = "springtorch-workflow-task-queue"
const val LLM_TASK_QUEUE = "llm-task-queue"

@ActivityInterface
interface LlmActivities {
    @ActivityMethod(name = "generate_qwen_response")
    fun generateQwenResponse(prompt: String): String
}

@WorkflowInterface
interface LlmWorkflow {
    @WorkflowMethod
    fun generateText(prompt: String): String
}

class LlmWorkflowImpl : LlmWorkflow {
    private val activities = Workflow.newActivityStub(
        LlmActivities::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(10))
            .setTaskQueue(LLM_TASK_QUEUE)
            .build(),
    )

    override fun generateText(prompt: String): String = activities.generateQwenResponse(prompt)
}
