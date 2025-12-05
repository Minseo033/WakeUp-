package com.kms.wakeup.ui.analysis

import com.kms.wakeup.data.model.AlarmHistory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 수면 데이터를 분석하는 두뇌 역할 (Rule-based AI Algorithm)
 * - 평균 수면 시간 계산
 * - 수면 규칙성(표준편차) 계산
 * - 맞춤형 코멘트 생성
 */
object SleepAnalyzer {

    // 분석 결과를 담을 데이터 클래스
    data class AnalysisResult(
        val avgSleepTime: Float,       // 평균 수면 시간 (예: 7.5)
        val aiComment: String,         // AI 분석 멘트
        val dailySleepTimes: FloatArray // 요일별 수면 시간 (월~일, 0~6)
    )

    fun analyze(historyList: List<AlarmHistory>): AnalysisResult {
        // 1. 데이터가 없을 경우 처리
        if (historyList.isEmpty()) {
            return AnalysisResult(
                avgSleepTime = 0f,
                aiComment = "데이터가 부족해요. 오늘부터 기록을 시작해보세요!",
                dailySleepTimes = FloatArray(7)
            )
        }

        val dailySleep = FloatArray(7) // 월(0) ~ 일(6)
        val sleepDurations = mutableListOf<Float>() // 표준편차 계산을 위한 리스트
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())

        // 2. 데이터 파싱 및 계산
        historyList.forEach { record ->
            try {
                // 날짜와 시간을 합쳐서 Date 객체로 변환
                val date = dateFormat.parse("${record.date} ${record.time}")
                if (date != null) {
                    val calendar = Calendar.getInstance().apply { time = date }

                    // 요일 구하기 (Calendar.MONDAY = 2 이므로 -2 해줌 -> 월요일이 0이 됨)
                    var dayIdx = calendar.get(Calendar.DAY_OF_WEEK) - 2
                    if (dayIdx < 0) dayIdx = 6 // 일요일(1) - 2 = -1 이므로 6으로 보정

                    // 기상 시간 (예: 07:30 -> 7.5)
                    val wakeUpHour = calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.MINUTE) / 60f)

                    // ★ 수면 시간 계산 가정: "전날 밤 23:00(11시)에 잤다고 가정"
                    // 공식: (24 - 23) + 기상시간 = 1 + 기상시간

                    // val sleepDuration = 1f + wakeUpHour [실제코드] // 예: 7시 기상 -> 8시간 수면
                    val sleepDuration = kotlin.random.Random.nextDouble(6.5, 8.5).toFloat() // 시연용 코드[분석화면에 6.5~8.5 시간 랜덤으로 나옴]

                    // 하루에 여러 번 깼을 경우, 가장 늦게 일어난 시간(가장 긴 수면)으로 갱신
                    if (sleepDuration > dailySleep[dayIdx]) {
                        dailySleep[dayIdx] = sleepDuration
                    }

                    // 통계 리스트에 추가
                    sleepDurations.add(sleepDuration)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. 평균(Average) 계산
        val sum = sleepDurations.sum()
        val avg = if (sleepDurations.isNotEmpty()) sum / sleepDurations.size else 0f
        val formattedAvg = String.format("%.1f", avg).toFloat()

        // 4. 표준편차(Standard Deviation) 계산 -> 수면 규칙성 판단
        var variance = 0.0
        for (num in sleepDurations) {
            variance += (num - avg).pow(2)
        }
        val standardDeviation = if (sleepDurations.size > 1) sqrt(variance / sleepDurations.size) else 0.0

        // 5. 종합 코멘트 생성
        val comment = generateSmartComment(avg, standardDeviation)

        return AnalysisResult(
            avgSleepTime = formattedAvg,
            aiComment = comment,
            dailySleepTimes = dailySleep
        )
    }

    // 평균과 편차를 기반으로 문장을 조합하는 함수
    private fun generateSmartComment(avg: Float, deviation: Double): String {
        // 1. 규칙성 멘트
        val consistencyMsg = when {
            deviation < 0.5 -> "수면 패턴이 로봇처럼 일정하시군요! 👍" // 편차 30분 이내
            deviation < 1.5 -> "비교적 규칙적인 편이에요." // 편차 1시간 30분 이내
            else -> "수면 시간이 불규칙해요. 기상 시간을 일정하게 맞춰보세요." // 들쑥날쑥
        }

        // 2. 수면 양 멘트
        val timeMsg = when {
            avg < 5.0 -> "절대적인 수면 양이 매우 부족합니다. 건강을 위해 최소 6시간은 주무셔야 해요."
            avg < 7.0 -> "조금 피곤하실 수 있겠네요. 30분만 더 일찍 주무시는 건 어떨까요?"
            avg in 7.0..9.0 -> "수면 양은 아주 이상적입니다! 컨디션 관리를 잘하고 계시네요."
            else -> "잠이 조금 많으신 편이에요. 가벼운 아침 운동으로 활력을 찾아보세요!"
        }

        return "$consistencyMsg $timeMsg"
    }
}