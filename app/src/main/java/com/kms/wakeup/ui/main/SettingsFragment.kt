package com.kms.wakeup.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kms.wakeup.R
import com.kms.wakeup.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // ================= [1. 데이터 관리] =================

        // ★ [추가됨] 나만의 문장 추가
        view.findViewById<LinearLayout>(R.id.btn_manage_sentences).setOnClickListener {
            showAddSentenceDialog()
        }

        // 수면 기록 초기화
        view.findViewById<LinearLayout>(R.id.btn_reset_history).setOnClickListener {
            showConfirmationDialog("수면 기록 삭제", "정말 모든 분석 데이터를 삭제하시겠습니까?") {
                resetHistory()
            }
        }

        // 앱 정보
        view.findViewById<LinearLayout>(R.id.btn_app_info).setOnClickListener {
            Toast.makeText(requireContext(), "WakeUp! v1.0.0 \nDeveloped by You", Toast.LENGTH_SHORT).show()
        }

        // ================= [2. 수면 목표] =================
        val seekbar = view.findViewById<SeekBar>(R.id.seekbar_sleep_goal)
        val tvGoal = view.findViewById<TextView>(R.id.tv_sleep_goal_value)

        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedGoal = prefs.getInt("sleep_goal", 8)
        seekbar.progress = savedGoal
        tvGoal.text = savedGoal.toString()

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                tvGoal.text = progress.toString()
                prefs.edit().putInt("sleep_goal", progress).apply()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        // ================= [3. 미션 난이도 설정] =================
        val btnEasy = view.findViewById<TextView>(R.id.btn_easy)
        val btnNormal = view.findViewById<TextView>(R.id.btn_normal)
        val btnHard = view.findViewById<TextView>(R.id.btn_hard)
        val tvDesc = view.findViewById<TextView>(R.id.tv_difficulty_desc)
        val tvStatus = view.findViewById<TextView>(R.id.tv_difficulty_status)

        val buttons = listOf(btnEasy, btnNormal, btnHard)

        fun updateDifficulty(selected: TextView, diffName: String, desc: String, code: String) {
            buttons.forEach {
                it.isSelected = false
                it.setTextColor(Color.parseColor("#212121"))
            }
            selected.isSelected = true
            selected.setTextColor(Color.WHITE)

            tvStatus.text = diffName
            tvDesc.text = desc
            prefs.edit().putString("mission_difficulty", code).apply()
        }

        btnEasy.setOnClickListener { updateDifficulty(btnEasy, "쉬움", "가벼운 미션으로 상쾌하게 일어납니다", "easy") }
        btnNormal.setOnClickListener { updateDifficulty(btnNormal, "보통", "적당한 난이도의 미션이 주어집니다", "normal") }
        btnHard.setOnClickListener { updateDifficulty(btnHard, "어려움", "미션 난이도가 높아집니다. 잠이 깰 수밖에 없습니다!", "hard") }

        val savedDifficulty = prefs.getString("mission_difficulty", "hard") ?: "hard"
        when(savedDifficulty) {
            "easy" -> updateDifficulty(btnEasy, "쉬움", "가벼운 미션으로 상쾌하게 일어납니다", "easy")
            "normal" -> updateDifficulty(btnNormal, "보통", "적당한 난이도의 미션이 주어집니다", "normal")
            else -> updateDifficulty(btnHard, "어려움", "미션 난이도가 높아집니다. 잠이 깰 수밖에 없습니다!", "hard")
        }

        return view
    }

    // ★ [추가됨] 문장 입력 다이얼로그
    private fun showAddSentenceDialog() {
        val input = EditText(requireContext())
        input.hint = "힘이 되는 문장을 입력하세요"
        val padding = (16 * resources.displayMetrics.density).toInt()
        input.setPadding(padding, padding, padding, padding)

        AlertDialog.Builder(requireContext())
            .setTitle("나만의 문장 추가")
            .setView(input)
            .setPositiveButton("추가") { _, _ ->
                val text = input.text.toString()
                if (text.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        AlarmRepository.addCustomSentence(requireContext(), text)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "문장이 추가되었습니다!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNeutralButton("초기화 (전체 삭제)") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    AlarmRepository.clearCustomSentences(requireContext())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "커스텀 문장이 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("삭제") { _, _ -> onConfirm() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun resetHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AlarmRepository.clearAllHistory(requireContext())
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "모든 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}