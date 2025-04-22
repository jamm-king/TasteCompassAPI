package com.tastecompass.analyzer.service

import com.tastecompass.analyzer.client.OpenAIChatter
import com.tastecompass.analyzer.config.OpenAIConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[OpenAIConfig::class, OpenAIChatter::class, ReviewAnalyzer::class])
@TestPropertySource("classpath:openai.properties")
class ReviewAnalyzerTest {

    @Autowired
    lateinit var reviewAnalyzer: ReviewAnalyzer

    private val logger = LoggerFactory.getLogger(this::class.simpleName)
    private val review = """
        수원 영통 맛집 탑3 안에 들지 않을까 싶은,,
        진심 존맛 카이센동을 판매하는, 내 최애 식당인
        ‘좋은소식’
        방문일 : 주말 (토요일) 18시 (예약 방문)
        만족도 : 재방문 의사 매우 매우 있음
        수원 영통 좋은소식 매장 정보
        ✔️위치✔️
        영통역이랑은 조금 떨어져있지만
        걸어갈만한 거리에 있습니당ㅎㅎ
        50m
        © NAVER Corp.
        좋은소식
        경기도 수원시 영통구 매영로269번길 43 3층 302호 좋은소식
        ✔️영업시간✔️
        수목금(점심만영업) 11:30-15:00
        토요일 (점심저녁영업) 11:30-15:00 / 17:30-21:00
        포장 예약 단체 가능 / 주차 가능
        전화 예약 필수! (0507-1307-1228)
        ✔️매장외부✔️
        이런데 식당이? 할 법한 오피스 건물 3층에 있어옹
        엘리베이터 타고 올라가면 식당이 등장합니당ㅎㅎ
        ✔️주차장✔️
        건물 1층에 주차 불가하고
        건물 뒷편에 공간이나 영흥숲공원 제2주차장에 가능
        ✔️매장내부✔️
        문 열고 들어가면 아늑한 분위기가 물씬 느껴지고
        대기 공간이 따로 마련되어 있어용
        바 테이블도 있고 일반 테이블도 있습니당
        긴 테이블도 있어서 단체로 와도 좋음!
        ✔️화장실✔️
        매장 건물에 남/여 분리로 있었고 깨끗해서 굳
        수원 영통 좋은소식 메뉴 소개
        ✔️메뉴판✔️
        사시미와 덮밥 종류도 다양하게 판매 중!
        계절메뉴인 방어동도 판매하고 계십니당
        ✔️원산지✔️
        원산지가 궁금하신 분들은 참고!
        수원 영통 좋은소식 이용 후기
        ✔️주문한 메뉴✔️
        우니사케동 29,000
        우니이꾸라동 48,500
        코카콜라제로 2,500
        총 80,000!
        메뉴 주문해놓고 매장 구경하기 ㅎㅎ
        매장에 초밥왕 만화책이 있는거 넘 졸귀
        긴 테이블은 이렇게 물컵이 다양한 종류로 있어서
        물컵도 취향대로 고를 수 있어욤ㅎㅎ
        메뉴 나올 때까지 입이 심심하니까
        사장님께서 나눠주신 누룽지 ㅎㅎㅎ 센스 굿!
        그 다음 장국이 나오고~ 장국 들이키면서
        좋은 소식 즐기는 방법 정독해주면 됩니당ㅎㅎ
        좋은 소식 덮밥은 도시락처럼 회/밥이 따로 나와서
        회가 안따뜻해지고 좋음 ㅜㅜㅠ 사장님 센스 만점
        곧이어 등장한 우리의 메뉴ㅠㅠㅠ
        때깔 미쳤지 않나욤,,
        이렇게 회 / 밥이 나눠져있어용ㅎㅎ
        도시락 감성쓰
        밥은 초밥밥입니당!! 간이 딱~~
        회는 무채랑 오이가 들어가니 오이 싫어하시는 분들은
        미리 빼달라고 말하기~
        그나저나 우니사케동 비주얼 미쵸따ㅜ 으아아악
        한 점 한 점이 완전 두툼쓰 ㅜㅠ 숙성도 아주 잘됐음 ㅜ
        무게를 재서 횟감을 주시니까 양도 믿고 먹을 수 있음!
        요렇게 밥이 따로 있으니 
        사시미 느낌으로 와사비 간장에만 먹어두 존맛 ㅜ
        뱃살 녹진함 미쳐따,,
        요러케 초밥밥에 연어 우니 싹 올려먹으면 
        얼마나 맛있게유ㅜㅠㅜ나 울엉
        우니이꾸라동도 비주얼 미쳤지 않나요,,
        우니랑 이꾸라도 그득그득ㅜ 
        이정도양이면 가격 납득 쌉가능이쥬~
        이꾸라 (연어알)은 북해도산 최상급!
        이 날 우니는 멕시코산이라고 설명해주셨당ㅎㅎ
        최상급 연어알답게 알이 하나하나 엄청 큼..!
        일본 현지에서 먹었을 때보다 훨 커서 놀랐었당
        우니동 우니이꾸라동은 김이 기본으로 제공돼서
        이렇게 싸먹기 가능!!!!
        넘 녹진하고 존맛 ㅠㅠㅠㅠㅠ
        싹싹 비우고 배부르게 잘 먹고 갑니다아
        ✔️총평✔️
        역시 영통 최고 존엄 맛집 답게,,,
        횟감이며 밥이며 하나 같이 다 싱싱하고 푸짐 ㅠㅠ
        덮밥도 너무너무 정갈하고 이쁘게 나오고
        매장 분위기도 아늑해서 좋았던,,
        매장 영업시간이 한정적인게 좀 아쉽지만
        그래도 요 퀄리티를 유지하면서 운영하시려면
        완전 이해 가능합니다ㅠㅠ
        이렇게 오래오래 영업해주세요,,
        좋은소식 사랑햐,, 또또또 가야징 ㅜㅠ
        영통 최고 존맛집으로 강추강추!!
        [출처] 수원 영통 인생 카이센동 맛집 ‘좋은소식’ 내돈내산 후기|작성자 안전하자용
    """.trimIndent()

    @Test
    fun `should extract attributes from review`() = runBlocking {
        val json = reviewAnalyzer.analyze(review)
        logger.info(json.toString())
    }
}