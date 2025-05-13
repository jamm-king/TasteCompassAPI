package com.tastecompass.kakao.config

import com.tastecompass.kakao.common.KakaoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:kakao.properties")
@EnableConfigurationProperties(KakaoProperties::class)
class KakaoConfig