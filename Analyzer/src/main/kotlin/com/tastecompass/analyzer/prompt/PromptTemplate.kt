package com.tastecompass.analyzer.prompt

object PromptTemplate {
    fun forReviewAnalysis(review: String): String = """
        아래 블로그 리뷰를 참고하여 식당의 속성을 분석해줘.
        다음 항목들을 JSON 형식으로 추출해줘:
        - name: 식당 이름 (있으면)
        - category: 음식 종류 (예: 한식, 일식, 중식, 양식 등)
        - phone: 식당 전화번호 (예: 052-242-2401)
        - address: 식당 주소 (예: 포항시 북구 이인로 90)
        - businessDays: 영업일 또는 영업시간 정보
        - hasWifi: 와이파이 제공 여부 (true/false)
        - hasParking: 주차 가능 여부 (true/false)
        - menus: 메뉴 이름과 가격 (예: [{"name": "김치찌개", "price": 8000}])
        - minPrice: 메뉴 중 가장 저렴한 가격
        - maxPrice: 메뉴 중 가장 비싼 가격
        - taste: 맛에 대한 설명 (예: 매콤한 맛, 담백한 국물 등)
        - mood: 분위기에 대한 설명 (예: 조용한 분위기, 가족 단위에 적합 등)

        리뷰:
        "$review"

        형식:
        {
          "name": "...",
          "category": "...",
          "phone": "...",
          "address": "...",
          "businessDays": "...",
          "hasWifi": true,
          "hasParking": false,
          "menus": [{"name": "...", "price": 10000}],
          "minPrice": 10000,
          "maxPrice": 20000,
          "taste": "...",
          "mood": "...",
        }
    """.trimIndent()
}