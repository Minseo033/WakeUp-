package com.kms.wakeup.ui.main

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kms.wakeup.R
import com.kms.wakeup.alarm.AlarmScheduler
import com.kms.wakeup.data.AlarmRepository
import com.kms.wakeup.data.model.Alarm
import com.kms.wakeup.ui.addalarm.AddAlarmActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var headerTime: TextView
    private lateinit var headerDate: TextView
    private lateinit var alarmListContainer: LinearLayout
    private lateinit var addAlarmFab: FloatingActionButton

    private val handler = Handler(Looper.getMainLooper())
    private val timeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerTime = view.findViewById(R.id.header_time)
        headerDate = view.findViewById(R.id.header_date)
        alarmListContainer = view.findViewById(R.id.alarm_list_container)
        addAlarmFab = view.findViewById(R.id.add_alarm_fab)

        handler.post(timeRunnable)

        addAlarmFab.setOnClickListener {
            val intent = Intent(requireContext(), AddAlarmActivity::class.java)
            startActivity(intent)
        }

        loadAlarms()
    }

    override fun onResume() {
        super.onResume()
        loadAlarms()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timeRunnable)
    }

    private fun updateTime() {
        val now = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN)

        headerTime.text = timeFormat.format(now.time)
        headerDate.text = dateFormat.format(now.time)
    }

    private fun loadAlarms() {
        alarmListContainer.removeAllViews()
        val alarms: List<Alarm> = AlarmRepository.getAlarms(requireContext())
        val inflater = LayoutInflater.from(requireContext())

        for ((index, alarm) in alarms.withIndex()) {
            val card = inflater.inflate(R.layout.item_alarm, alarmListContainer, false)

            val timeText = card.findViewById<TextView>(R.id.alarm_time_text)
            val labelText = card.findViewById<TextView>(R.id.alarm_label_text)
            val missionChip = card.findViewById<TextView>(R.id.mission_chip)
            val switchView = card.findViewById<SwitchCompat>(R.id.alarm_switch)
            val deleteIcon = card.findViewById<ImageView>(R.id.delete_icon)
            val dayContainer = card.findViewById<LinearLayout>(R.id.day_container)

            // 1. 시간 및 라벨
            timeText.text = String.format("%02d:%02d", alarm.hour, alarm.minute)
            labelText.text = alarm.label.ifBlank { "알람" }

            // 2. 미션 칩 색상 설정
            val mission = alarm.mission
            if (mission.isNullOrBlank() || mission == "미션 없음") {
                missionChip.visibility = View.GONE
            } else {
                missionChip.visibility = View.VISIBLE
                missionChip.text = mission

                val (bgColor, textColor) = when (mission) {
                    "수학 문제" -> Pair(R.color.chip_bg_math, R.color.chip_text_math)
                    "폰 흔들기" -> Pair(R.color.chip_bg_shake, R.color.chip_text_shake)
                    "연타" -> Pair(R.color.chip_bg_tap, R.color.chip_text_tap)
                    "타자 입력" -> Pair(R.color.chip_bg_typing, R.color.chip_text_typing)
                    else -> Pair(R.color.chip_bg_none, R.color.chip_text_none)
                }

                missionChip.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bgColor))
                missionChip.setTextColor(ContextCompat.getColor(requireContext(), textColor))
            }

            // 3. 요일 칩
            dayContainer.removeAllViews()
            for (d in alarm.days) {
                val chip = TextView(requireContext()).apply {
                    text = d
                    textSize = 12f
                    setPadding(24, 12, 24, 12)
                    background = ContextCompat.getDrawable(context, R.drawable.bg_day_chip_small)
                    setTextColor(ContextCompat.getColor(context, R.color.day_chip_text_on))
                }

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 0, 12, 0)
                chip.layoutParams = lp

                dayContainer.addView(chip)
            }

            // 4. 스위치 & 삭제
            switchView.isChecked = alarm.isOn
            updateCardAlpha(card, alarm.isOn)

            switchView.setOnCheckedChangeListener { _, isChecked ->
                alarm.isOn = isChecked
                updateCardAlpha(card, isChecked)
                AlarmRepository.updateAlarm(requireContext(), alarm)

                if (isChecked) AlarmScheduler.register(requireContext(), alarm)
                else AlarmScheduler.cancel(requireContext(), alarm)
            }

            deleteIcon.setOnClickListener {
                AlarmScheduler.cancel(requireContext(), alarm)
                AlarmRepository.removeAlarm(requireContext(), alarm)
                loadAlarms()
            }

            // ★★★ [추가된 부분] 카드 전체 클릭 시 수정 화면 이동 ★★★
            card.setOnClickListener {
                val intent = Intent(requireContext(), AddAlarmActivity::class.java).apply {
                    putExtra("alarm_data", alarm) // Alarm 객체 전체를 넘김 (Serializable)
                }
                startActivity(intent)
            }

            alarmListContainer.addView(card)
        }
    }

    private fun updateCardAlpha(view: View, isEnabled: Boolean) {
        view.alpha = if (isEnabled) 1.0f else 0.5f
    }
}