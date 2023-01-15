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
package io.infinitic.storage.redis

import io.infinitic.storage.config.Redis
import io.infinitic.storage.config.redis.RedisKeyValueStorage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import redis.embedded.RedisServer

class RedisKeyValueStorageTests :
    StringSpec({
      val redisServer = RedisServer(6380).also { it.start() }
      val storage = RedisKeyValueStorage.of(Redis("localhost", 6380))

      afterSpec {
        Redis.close()
        redisServer.stop()
      }

      beforeTest { storage.put("foo", "bar".toByteArray()) }

      afterTest { storage.flush() }

      "getValue should return null on unknown key" { storage.get("unknown") shouldBe null }

      "getValue should return value" {
        storage.get("foo").contentEquals("bar".toByteArray()) shouldBe true
      }

      "putValue on new key should create value" {
        storage.put("foo2", "bar2".toByteArray())

        storage.get("foo2").contentEquals("bar2".toByteArray()) shouldBe true
      }

      "putValue on existing key should update value" {
        storage.put("foo", "bar2".toByteArray())

        storage.get("foo").contentEquals("bar2".toByteArray()) shouldBe true
      }

      "delValue on unknown key does nothing" { storage.del("unknown") }

      "delValue should delete value" {
        storage.del("foo")

        storage.get("foo") shouldBe null
      }
    })
