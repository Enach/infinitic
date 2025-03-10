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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    gradlePluginPortal()
    maven(url = "https://dl.bintray.com/gradle/gradle-plugins")
  }
}

plugins {
  id(Plugins.Kotlin.id) version Plugins.Kotlin.version
  id(Plugins.Serialization.id) version Plugins.Serialization.version apply false
  id(Plugins.Ktfmt.id) version Plugins.Ktfmt.version apply true
  id(Plugins.TestLogger.id) version Plugins.TestLogger.version apply true
}

repositories { mavenCentral() }

subprojects {
  apply(plugin = Plugins.Kotlin.id)
  apply(plugin = Plugins.Serialization.id)
  apply(plugin = Plugins.Ktfmt.id)
  apply(plugin = Plugins.TestLogger.id)

  group = Libs.org
  version = Ci.version

  repositories { mavenCentral() }

  dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(Libs.Logging.api)

    testImplementation(Libs.Slf4j.simple)
    testImplementation(Libs.Kotest.junit5)
    testImplementation(Libs.Kotest.property)
    testImplementation(Libs.Mockk.mockk)

    if (name != "infinitic-common") {
      testImplementation(testFixtures(project(":infinitic-common")))
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()

    testlogger { showFullStackTraces = true }
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
      jvmTarget = JavaVersion.VERSION_11.toString()
    }
  }

  // Keep this to tell compatibility to applications
  tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
  }
}
