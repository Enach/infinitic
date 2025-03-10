/**
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including
 * without limitation fees for hosting or consulting/ support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from the functionality of the
 * Software. Any license notice or attribution required by the License must also include this
 * Commons Clause License Condition notice.
 *
 * Software: Infinitic
 *
 * License: MIT License (https://opensource.org/licenses/MIT)
 *
 * Licensor: infinitic.io
 */
package io.infinitic.common.workflows.engine.messages

import com.github.avrokotlin.avro4k.AvroName
import com.github.avrokotlin.avro4k.AvroNamespace
import io.infinitic.common.data.ClientName
import io.infinitic.common.data.MessageId
import io.infinitic.common.data.ReturnValue
import io.infinitic.common.data.methods.MethodName
import io.infinitic.common.data.methods.MethodParameterTypes
import io.infinitic.common.data.methods.MethodParameters
import io.infinitic.common.messages.Message
import io.infinitic.common.tasks.data.ServiceName
import io.infinitic.common.tasks.data.TaskId
import io.infinitic.common.tasks.data.TaskReturnValue
import io.infinitic.common.tasks.executors.errors.CanceledTaskError
import io.infinitic.common.tasks.executors.errors.CanceledWorkflowError
import io.infinitic.common.tasks.executors.errors.DeferredError
import io.infinitic.common.tasks.executors.errors.FailedTaskError
import io.infinitic.common.tasks.executors.errors.FailedWorkflowError
import io.infinitic.common.tasks.executors.errors.UnknownWorkflowError
import io.infinitic.common.workflows.data.channels.ChannelName
import io.infinitic.common.workflows.data.channels.ChannelType
import io.infinitic.common.workflows.data.channels.SignalData
import io.infinitic.common.workflows.data.channels.SignalId
import io.infinitic.common.workflows.data.methodRuns.MethodRunId
import io.infinitic.common.workflows.data.timers.TimerId
import io.infinitic.common.workflows.data.workflowTasks.WorkflowTask
import io.infinitic.common.workflows.data.workflows.WorkflowCancellationReason
import io.infinitic.common.workflows.data.workflows.WorkflowId
import io.infinitic.common.workflows.data.workflows.WorkflowMeta
import io.infinitic.common.workflows.data.workflows.WorkflowName
import io.infinitic.common.workflows.data.workflows.WorkflowReturnValue
import io.infinitic.common.workflows.data.workflows.WorkflowTag
import io.infinitic.workflows.DeferredStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface WorkflowEvent

interface MethodEvent : WorkflowEvent {
  val methodRunId: MethodRunId
}

interface TaskEvent : MethodEvent {
  fun taskId(): TaskId
  fun serviceName(): ServiceName
}

@Serializable
sealed class WorkflowEngineMessage : Message {
  val messageId: MessageId = MessageId()
  abstract val emitterName: ClientName
  abstract val workflowId: WorkflowId
  abstract val workflowName: WorkflowName

  override fun envelope() = WorkflowEngineEnvelope.from(this)

  fun isWorkflowTaskEvent() =
      (this is TaskEvent) && this.serviceName() == ServiceName(WorkflowTask::class.java.name)
}

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class DispatchWorkflow(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    val methodName: MethodName,
    val methodParameters: MethodParameters,
    val methodParameterTypes: MethodParameterTypes?,
    val workflowTags: Set<WorkflowTag>,
    val workflowMeta: WorkflowMeta,
    var parentWorkflowName: WorkflowName?,
    var parentWorkflowId: WorkflowId?,
    var parentMethodRunId: MethodRunId?,
    val clientWaiting: Boolean,
    override val emitterName: ClientName
) : WorkflowEngineMessage()

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class RetryWorkflowTask(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val emitterName: ClientName
) : WorkflowEngineMessage()

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class RetryTasks(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    val taskId: TaskId?,
    val taskStatus: DeferredStatus?,
    @SerialName("taskName") val serviceName: ServiceName?,
    override val emitterName: ClientName
) : WorkflowEngineMessage()

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class CompleteTimers(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    val methodRunId: MethodRunId?,
    override val emitterName: ClientName
) : WorkflowEngineMessage()

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class WaitWorkflow(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    val methodRunId: MethodRunId,
    override val emitterName: ClientName
) : WorkflowEngineMessage()

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class DispatchMethod(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    val methodRunId: MethodRunId,
    val methodName: MethodName,
    val methodParameters: MethodParameters,
    val methodParameterTypes: MethodParameterTypes?,
    var parentWorkflowId: WorkflowId?,
    var parentWorkflowName: WorkflowName?,
    var parentMethodRunId: MethodRunId?,
    val clientWaiting: Boolean,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), WorkflowEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class CancelWorkflow(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    val methodRunId: MethodRunId?,
    @AvroNamespace("io.infinitic.workflows.data") val reason: WorkflowCancellationReason,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), WorkflowEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class CompleteWorkflow(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    val workflowReturnValue: ReturnValue,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), WorkflowEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class SendSignal(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    val channelName: ChannelName,
    @AvroName("channelSignalId") val signalId: SignalId,
    @AvroName("channelSignal") val signalData: SignalData,
    @AvroName("channelSignalTypes") val channelTypes: Set<ChannelType>,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), WorkflowEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class TimerCompleted(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val methodRunId: MethodRunId,
    val timerId: TimerId,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), MethodEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class ChildMethodUnknown(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val methodRunId: MethodRunId,
    val childUnknownWorkflowError: UnknownWorkflowError,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), MethodEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class ChildMethodCanceled(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val methodRunId: MethodRunId,
    val childCanceledWorkflowError: CanceledWorkflowError,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), MethodEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class ChildMethodFailed(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val methodRunId: MethodRunId,
    val childFailedWorkflowError: FailedWorkflowError,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), MethodEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class ChildMethodCompleted(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val methodRunId: MethodRunId,
    val childWorkflowReturnValue: WorkflowReturnValue,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), MethodEvent

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class TaskCanceled(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val methodRunId: MethodRunId,
    val canceledTaskError: CanceledTaskError,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), TaskEvent {
  override fun taskId() = canceledTaskError.taskId
  override fun serviceName() = canceledTaskError.serviceName
}

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class TaskFailed(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val methodRunId: MethodRunId,
    val failedTaskError: FailedTaskError,
    val deferredError: DeferredError?,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), TaskEvent {
  override fun taskId() = failedTaskError.taskId
  override fun serviceName() = failedTaskError.serviceName
}

@Serializable
@AvroNamespace("io.infinitic.workflows.engine")
data class TaskCompleted(
    override val workflowName: WorkflowName,
    override val workflowId: WorkflowId,
    override val methodRunId: MethodRunId,
    val taskReturnValue: TaskReturnValue,
    override val emitterName: ClientName
) : WorkflowEngineMessage(), TaskEvent {
  override fun taskId() = taskReturnValue.taskId
  override fun serviceName() = taskReturnValue.serviceName
}
