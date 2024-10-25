package com.service

import com.entity.SampleData
import com.service.repository.SampleRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import java.util.logging.Logger

class SampleProcessor : Processor {
    private var scope: CoroutineScope? = null
    override fun start(parentScope: CoroutineScope) {
        logger.info("start")

        scope = scope?.let {
            if(!it.isActive) {
                CoroutineScope(Dispatchers.IO)
            }
            it
        } ?: CoroutineScope(Dispatchers.IO)

        parentScope.launch {
            scope?.launch {
                val getData = fetchData(SampleRepository())
                val analyzeData = analyze(getData)
                store(analyzeData)
            } ?: logger.warning("scope is null")
        }
    }

    override fun stop() {
        logger.info("stop")
        scope?.cancel()
    }

    private fun CoroutineScope.fetchData(repository: SampleRepository): ReceiveChannel<SampleData> = produce {
        repository.getAsFlow().collect {
            logger.info("collect data - ${it.sample}")
            send(it)
        }
    }

    private fun CoroutineScope.analyze(channel: ReceiveChannel<SampleData>) = produce {
        channel.consumeEach { sample ->
            logger.info("analyzing [${sample.sample}]")
            scope?.run {
                val result = DefaultAnalyzer(this).analyze(sample.sample)
                logger.info("process pipeline done! [${sample.sample}] $result")
                send(sample.sample)
            }
        }
    }

    private fun CoroutineScope.store(channel: ReceiveChannel<String>) = launch {
        channel.consumeEach { result ->
            logger.info("storing [${result}]")
            scope?.run {
                SampleRepository().insert()
            }
        }
    }

    companion object {
        private const val TAG = "SampleProcessor"
        private val logger = Logger.getLogger(TAG)
    }
}