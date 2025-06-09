package com.tastecompass.analyzer.prompt

object PromptTemplate {
    fun forReviewAnalysis(review: String): String = """
        <system>
            당신은 식당 리뷰에서 구조화된 정보를 추출하는 JSON 생성기입니다.
            아래 스키마에 맞춰 유효한 JSON만 출력하세요. 절대 다른 텍스트나 설명을 추가하지 마세요. 확실한 정보만 추출해주세요.
            
            스키마:
            {
              "name": string,            // 식당 이름
              "category": string,        // 음식 종류
              "phone": string,           // 전화번호
              "address": string,         // 주소 (도로명 또는 지번)
              "businessDays": string,    // 영업일/시간
              "hasWifi": boolean,        // 와이파이 제공 여부
              "hasParking": boolean,     // 주차 가능 여부
              "menus": [                 // 메뉴 목록
                {
                  "name": string,        // 메뉴 이름
                  "price": integer       // 가격 (정수)
                }
              ],
              "minPrice": integer,       // 최소 가격
              "maxPrice": integer,       // 최대 가격
              "taste": string,           // 맛 설명
              "mood": string             // 분위기 설명
            }
        </system>
        <user>
            아래 블로그 리뷰를 읽고, 위 스키마에 맞춰 JSON을 반환하세요.
            
            리뷰:
            $review
        </user>
    """.trimIndent()

    fun forQueryAnalysis(query: String): String = """
        <system>
            당신은 식당에 대한 설명에서 구조화된 정보를 추출하는 JSON 생성기입니다.
            아래 스키마에 맞춰 유효한 JSON만 출력하세요. 절대 다른 텍스트나 설명을 추가하지 마세요. 확실한 정보만 추출해주세요.
            
            스키마:
            {
              "taste": string,           // 맛 설명
              "mood": string,            // 분위기 설명
              "category": string         // 음식 종류
            }
        </system>
        <user>
            아래 쿼리를 읽고, 위 스키마에 맞춰 JSON을 반환하세요.
            
            쿼리:
            $query
        </user>
    """.trimIndent()
}