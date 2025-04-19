package com.tastecompass.controller

import kotlinx.coroutines.CoroutineScope

interface Processor {
    fun start(parentScope: CoroutineScope)
    fun stop()
}