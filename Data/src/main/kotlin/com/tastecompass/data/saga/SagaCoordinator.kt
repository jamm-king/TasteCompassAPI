package com.tastecompass.data.saga

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SagaCoordinator {

    suspend fun execute(ctx: SagaContext, steps: List<SagaStep<*>>) {
        val sagaId = ctx.sagaId
        val completed = mutableListOf<Pair<SagaStep<Any?>, Any?>>()

        try {
            for(raw in steps) {
                val step = raw as SagaStep<Any?>
                logger.debug("[$sagaId] STEP START: ${step.name}")
                val result = step.action(ctx)
                completed += step to result
                logger.debug("[$sagaId] STEP SUCCESS: ${step.name}")
            }
        } catch(e: Exception) {
            logger.error("[$sagaId] STEP Failed: ${e.message}. Starting compensation...")
            for((step, result) in completed.asReversed()) {
                try {
                    logger.debug("[$sagaId] COMPENSATE START: ${step.name}")
                    step.compensation(ctx, result)
                    logger.debug("[$sagaId] COMPENSATE SUCCESS: ${step.name}")
                } catch(c: Exception) {
                    logger.error("[$sagaId] COMPENSATE FAILED: ${step.name}: ${c.message}")
                }
            }
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}