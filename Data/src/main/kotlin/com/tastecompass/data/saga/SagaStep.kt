package com.tastecompass.data.saga

data class SagaStep<T>(
    val name: String,
    val action: suspend (ctx: SagaContext) -> T,
    val compensation: suspend (ctx: SagaContext, result: T) -> Unit
)
