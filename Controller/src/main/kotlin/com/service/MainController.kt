package com.service

import kotlinx.coroutines.*
import java.util.logging.Logger

class MainController : Controller{
    private var processScope = CoroutineScope(Job() + Dispatchers.Default)
    private val sampleProcessor = SampleProcessor()
    override fun start() {
        if(!processScope.isActive) {
            processScope = CoroutineScope(Job() + Dispatchers.Default)
        }
        processScope.launch {
            sampleProcessor.start(processScope)
            delay(60 * 1000)
            sampleProcessor.stop()
        }
    }

    override fun stop() {
        processScope.cancel()
    }

    companion object {
        private const val TAG = "SampleProcessor"
        private val logger = Logger.getLogger(TAG)
    }
}