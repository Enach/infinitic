package io.infinitic.taskManager.engine.pulsar.functions

import io.infinitic.taskManager.engine.avroClasses.AvroTaskEngine
import io.infinitic.taskManager.messages.envelopes.AvroEnvelopeForTaskEngine
import io.infinitic.taskManager.engine.pulsar.storage.PulsarAvroStorage
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import org.apache.pulsar.functions.api.Context
import java.util.Optional

class TaskEnginePulsarFunctionTests : StringSpec({
    "TaskEnginePulsarFunction should throw an exception if called without context" {
        // given
        val engine = TaskEnginePulsarFunction()
        // then
        shouldThrowAny {
            engine.process(mockk<AvroEnvelopeForTaskEngine>(), null)
        }
    }

    "TaskEnginePulsarFunction should call engine with correct parameters" {
        // mocking
        val topicPrefixValue = mockk<Optional<Any>>()
        every { topicPrefixValue.isPresent } returns false
        val context = mockk<Context>()
        every { context.logger } returns mockk<org.slf4j.Logger>(relaxed = true)
        every { context.getUserConfigValue("topicPrefix") } returns topicPrefixValue
        val engineFunction = spyk(AvroTaskEngine())
        coEvery { engineFunction.handle(any()) } just Runs
        val avroMsg = mockk<AvroEnvelopeForTaskEngine>()
        // given
        val fct = TaskEnginePulsarFunction()
        fct.engine = engineFunction
        // when
        fct.process(avroMsg, context)
        // then
        engineFunction.logger shouldBe context.logger
        (engineFunction.avroStorage as PulsarAvroStorage).context shouldBe context
        coVerify(exactly = 1) { engineFunction.handle(avroMsg) }
    }
})
