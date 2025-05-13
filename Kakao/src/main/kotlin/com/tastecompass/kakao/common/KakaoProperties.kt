package com.tastecompass.kakao.common

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix="kakao")
class KakaoProperties {
    lateinit var apiKey: String
}