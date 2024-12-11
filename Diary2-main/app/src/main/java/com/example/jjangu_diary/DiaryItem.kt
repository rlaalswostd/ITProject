package com.example.jjangu_diary

// 데이터 클래스는 일기 항목의 속성을 정의하는 클래스입니다.
data class DiaryItem(
    var id: String = "", // 일기 항목의 고유 ID (문자열 형식)
    var title: String = "", // 일기 제목 (문자열 형식)
    var content: String = "", // 일기 내용 (문자열 형식)
    var regdate: String = "", // 일기 작성 날짜 (문자열 형식)
    var weather: String = "", // 날씨 정보 (문자열 형식)
    var imageUrl: String = "" // 이미지 URL (문자열 형식), 일기에 첨부된 이미지의 URL
)
