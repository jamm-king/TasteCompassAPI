package com.tastecompass.kakao.dto

import com.google.gson.annotations.SerializedName

data class KakaoAddressResponse(
    @SerializedName("documents")
    val documents: List<Document>
)

data class Document(
    @SerializedName("road_address")
    val roadAddress: RoadAddress?,
    @SerializedName("address")
    val address: Address?
)

data class RoadAddress(
    @SerializedName("address_name")
    val addressName: String
)

data class Address(
    @SerializedName("x")
    val x: String,
    @SerializedName("y")
    val y: String
)