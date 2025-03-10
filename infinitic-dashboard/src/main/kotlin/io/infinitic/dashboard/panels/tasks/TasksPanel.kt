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
package io.infinitic.dashboard.panels.tasks

import io.infinitic.dashboard.Panel
import io.infinitic.dashboard.menus.TaskMenu
import kweb.Element
import kweb.ElementCreator
import kweb.div
import kweb.h1
import kweb.new

object TasksPanel : Panel() {
  override val menu = TaskMenu
  override val url = "/tasks"

  override fun render(creator: ElementCreator<Element>): Unit =
      with(creator) {
        div().classes("py-6").new {
          div().classes("max-w-7xl mx-auto px-4 sm:px-6 md:px-8").new {
            h1().classes("text-2xl font-semibold text-gray-900").text("Tasks")
          }
          div().classes("max-w-7xl mx-auto px-4 sm:px-6 md:px-8").new {
            div().classes("py-4").new {
              div().classes("border-4 border-dashed border-gray-200 rounded-lg h-96")
            }
          }
        }
      }
}
