package com.zenaton.jobManager.client

import com.zenaton.common.data.SerializedData
import com.zenaton.jobManager.data.JobId
import com.zenaton.jobManager.data.JobInput
import com.zenaton.jobManager.data.JobMeta
import com.zenaton.jobManager.data.JobName
import com.zenaton.jobManager.messages.DispatchJob
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ProxyHandler(private val className: String, private val dispatcher: Dispatcher) : InvocationHandler {
    private var jobId: JobId? = null

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>) {
        jobId = JobId()
        val msg = DispatchJob(
            jobId = jobId!!,
            jobName = JobName("$className::${method.name}"),
            jobInput = JobInput(args.map { SerializedData.from(it) }),
            jobMeta = JobMeta(mapOf("javaParameterTypes" to SerializedData.from(method.parameterTypes.map { it.name })))
        )
        dispatcher.toJobEngine(msg)
    }

    fun getJob() = jobId?.let { Job(it) }
}
