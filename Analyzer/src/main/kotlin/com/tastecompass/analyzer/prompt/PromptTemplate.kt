package com.tastecompass.analyzer.prompt

object PromptTemplate {
    fun forReviewAnalysis(review: String): String = """
    <system>
        당신은 식당 리뷰에서 구조화된 정보를 추출하는 JSON 생성기입니다.
        아래 스키마에 맞춰 유효한 JSON만 출력하세요. 절대 다른 텍스트나 설명을 추가하지 마세요. 확실한 정보만 추출해주세요.

        각 항목에 대해 정보가 없는 경우에는 반드시 다음과 같은 기본값으로 설정하세요:
        - string 타입: 빈 문자열 "" 
        - integer 타입: 0 
        - boolean 타입: false 
        - list (배열): 빈 배열 []

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

        각 항목(taste, mood, category)은 반드시 **한국어로 출력하세요**. 절대로 영어 단어로 출력하지 마세요.

        confidence 항목은 다음 기준에 따라 출력하세요:
        - 정보가 명확하고 확신이 높으면 → 0.9 ~ 1.0
        - 정보가 다소 유추된 경우 → 0.5 ~ 0.8
        - 정보가 거의 없거나 불확실하면 → 0.0 ~ 0.4

        intent 항목은 아래 값 중 하나로 설정하세요:
        - CATEGORY_FOCUSED : 음식 종류가 쿼리에서 중요하게 언급됨
        - MOOD_FOCUSED : 분위기가 쿼리에서 중요하게 언급됨
        - TASTE_FOCUSED : 맛이 쿼리에서 중요하게 언급됨
        - GENERIC : 특정 항목보다 전반적인 추천 의도가 강함

        스키마:
        {
          "taste": string (반드시 한국어로 출력하세요),
          "tasteConfidence": float,
          "mood": string (반드시 한국어로 출력하세요),
          "moodConfidence": float,
          "category": string (반드시 한국어로 출력하세요),
          "categoryConfidence": float,
          "intent": string (CATEGORY_FOCUSED | MOOD_FOCUSED | TASTE_FOCUSED | GENERIC)
        }
    </system>
    <user>
        아래 쿼리를 읽고, 위 스키마에 맞춰 JSON을 반환하세요.

        쿼리:
        $query
    </user>
""".trimIndent()
}