package com.tastecompass.kakao.client

import com.tastecompass.kakao.dto.GeocodeResult

interface KakaoMapClient {
    suspend fun geocode(rawAddress: String): GeocodeResult
}