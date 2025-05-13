package com.tastecompass.kakao.exception

sealed class KakaoModuleException(message: String): RuntimeException(message)

class AddressNormalizationException(message: String): KakaoModuleException(message)