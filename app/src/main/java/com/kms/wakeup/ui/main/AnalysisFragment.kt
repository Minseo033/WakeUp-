package com.kms.wakeup.ui.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kms.wakeup.R
import com.kms.wakeup.data.AlarmRepository
// SleepAnalyzer는 같은 패키지(ui.analysis)라 import 안 해도 되지만, 명시적으로 해도 됨

class AnalysisFragment : Fragment() {

    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_analysis, container, false)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        // 화면이 보일 때마다 최신 데이터로 갱신
        loadAndAnalyzeData()
    }

    private fun loadAndAnalyzeData() {
        val view = rootView ?: return
        val context = context ?: return

        // 1. DB에서 기상 기록 가져오기
        val historyList = AlarmRepository.getHistoryList(context)

        // 2. ★수정됨: 내부 함수 대신 'SleepAnalyzer' 객체에게 분석 요청
        val result = SleepAnalyzer.analyze(historyList)

        // 3. UI 업데이트: 평균 수면 시간
        val tvAvg = view.findViewById<TextView>(R.id.tv_avg_value)
        if (tvAvg != null) {
            tvAvg.text = result.avgSleepTime.toString()
        }

        // 4. UI 업데이트: AI 분석 멘트
        val tvAi = view.findViewById<TextView>(R.id.tv_ai_comment)
        if (tvAi != null) {
            tvAi.text = result.aiComment
        }

        // 5. 그래프 그리기 (수면 시간)
        val days = listOf("월", "화", "수", "목", "금", "토", "일")
        val barIds = listOf(
            R.id.bar_mon, R.id.bar_tue, R.id.bar_wed, R.id.bar_thu,
            R.id.bar_fri, R.id.bar_sat, R.id.bar_sun
        )

        // 그래프 최대 높이 기준 (가장 긴 막대가 꽉 차게)
        val maxSleep = result.dailySleepTimes.maxOrNull() ?: 1f

        for (i in 0..6) {
            val sleepTime = result.dailySleepTimes[i]
            // 높이 계산 (값이 있으면 최소 10dp는 보이게)
            val heightDp = if (sleepTime > 0) (sleepTime * 15).toInt() else 5
            setupBar(view, barIds[i], days[i], heightDp)
        }

        // 6. 그래프 그리기 (깬 횟수 - 예시로 랜덤값 적용, 실제 데이터 있으면 교체)
        val orangeBarIds = listOf(
            R.id.orange_bar_mon, R.id.orange_bar_tue, R.id.orange_bar_wed, R.id.orange_bar_thu,
            R.id.orange_bar_fri, R.id.orange_bar_sat, R.id.orange_bar_sun
        )
        for (i in 0..6) {
            // 깬 횟수 데이터가 없으므로 수면 기록이 있는 날만 랜덤 표시 (시각적 확인용)
            val wakeCount = if (result.dailySleepTimes[i] > 0) (0..3).random() else 0
            setupBar(view, orangeBarIds[i], days[i], wakeCount * 30) // 1회당 30dp 높이
        }
    }

    // 막대 그래프 하나를 세팅하는 함수
    private fun setupBar(rootView: View, includeId: Int, dayLabel: String, heightDp: Int) {
        val container = rootView.findViewById<View>(includeId) ?: return
        val barView = container.findViewById<View>(R.id.bar_view)
        val dayText = container.findViewById<TextView>(R.id.day_text)

        dayText.text = dayLabel

        val params = barView.layoutParams
        val density = resources.displayMetrics.density
        // 높이 적용 (dp -> px 변환)
        params.height = (heightDp * density).toInt()
        barView.layoutParams = params
    }

    // ★ 중요: 중복되었던 내부 클래스(AnalysisResult)와 함수(analyzeSleepData)를 모두 삭제했습니다.
    // 이제 SleepAnalyzer.kt의 로직을 가져다 씁니다.
}