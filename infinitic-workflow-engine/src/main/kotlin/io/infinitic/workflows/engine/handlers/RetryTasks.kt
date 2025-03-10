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
package io.infinitic.workflows.engine.handlers

import io.infinitic.common.workflows.data.commands.CommandId
import io.infinitic.common.workflows.data.commands.CommandStatus
import io.infinitic.common.workflows.data.commands.DispatchTaskPastCommand
import io.infinitic.common.workflows.engine.messages.RetryTasks
import io.infinitic.common.workflows.engine.state.WorkflowState
import io.infinitic.workflows.DeferredStatus
import io.infinitic.workflows.engine.helpers.dispatchTask
import io.infinitic.workflows.engine.output.WorkflowEngineOutput
import kotlinx.coroutines.CoroutineScope

internal fun CoroutineScope.retryTasks(
    output: WorkflowEngineOutput,
    state: WorkflowState,
    message: RetryTasks
) {
  val taskId = message.taskId?.run { CommandId.from(this) }
  val taskStatus =
      when (message.taskStatus) {
        DeferredStatus.ONGOING -> CommandStatus.Ongoing::class
        DeferredStatus.UNKNOWN -> CommandStatus.Unknown::class
        DeferredStatus.CANCELED -> CommandStatus.Canceled::class
        DeferredStatus.FAILED -> CommandStatus.Failed::class
        DeferredStatus.COMPLETED -> CommandStatus.Completed::class
        null -> null
      }
  val serviceName = message.serviceName

  // for all method runs
  state.methodRuns.forEach { methodRun ->
    // for all past tasks
    methodRun.pastCommands
        .filterIsInstance<DispatchTaskPastCommand>()
        .filter {
          (taskId == null || it.commandId == taskId) &&
              (taskStatus == null || it.commandStatus::class == taskStatus) &&
              (serviceName == null || it.command.serviceName == serviceName)
        }
        // dispatch a new sequence of those task
        .forEach { dispatchTaskPastCommand ->
          dispatchTaskPastCommand.taskRetrySequence = dispatchTaskPastCommand.taskRetrySequence + 1

          dispatchTask(output, state, dispatchTaskPastCommand)

          dispatchTaskPastCommand.commandStatus = CommandStatus.Ongoing
        }
  }
}
