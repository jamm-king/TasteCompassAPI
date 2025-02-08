package com.tastecompass.controller

import kotlinx.coroutines.CoroutineScope

interface Processor {
    fun start(scope: CoroutineScope)
    fun stop()
}