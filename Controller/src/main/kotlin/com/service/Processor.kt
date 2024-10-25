package com.service

import kotlinx.coroutines.CoroutineScope

interface Processor {
    fun start(scope: CoroutineScope)
    fun stop()
}