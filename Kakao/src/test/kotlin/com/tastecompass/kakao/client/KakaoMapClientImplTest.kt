package com.tastecompass.kakao.client

import com.tastecompass.kakao.config.KakaoConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[KakaoConfig::class, KakaoMapClientImpl::class])
class KakaoMapClientImplTest {

    @Autowired
    lateinit var client: KakaoMapClient

    @Test
    fun `should return GeocodeResult with given raw address`() = runBlocking {
        // given
        val rawAddress = "서울 강동구 강동대로 143-64 스퀘어100, 1층"

        // when
        val geocodeResult = client.geocode(rawAddress)

        // then
        logger.info("address : ${geocodeResult.normalizedAddress}")
        logger.info("x : ${geocodeResult.x}")
        logger.info("y : ${geocodeResult.y}")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}