package com.kms.wakeup.ui.addalarm

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kms.wakeup.R
import com.kms.wakeup.alarm.AlarmScheduler
import com.kms.wakeup.data.AlarmRepository
import com.kms.wakeup.data.model.Alarm

class AddAlarmActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var labelInput: EditText
    private lateinit var missionGroup: RadioGroup
    private lateinit var saveAlarmButton: Button

    private lateinit var monCheck: CheckBox
    private lateinit var tueCheck: CheckBox
    private lateinit var wedCheck: CheckBox
    private lateinit var thuCheck: CheckBox
    private lateinit var friCheck: CheckBox
    private lateinit var satCheck: CheckBox
    private lateinit var sunCheck: CheckBox

    private lateinit var btnRingtoneSelect: View
    private lateinit var tvRingtoneName: TextView
    private var selectedRingtoneUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    // ★ [추가]
    private lateinit var checkCustomSentence: CheckBox

    private var editAlarm: Alarm? = null

    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            selectedRingtoneUri = uri
            updateRingtoneName()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarm)

        val mainLayout = findViewById<android.view.View>(R.id.main_layout)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        editAlarm = intent.getSerializableExtra("alarm_data") as? Alarm

        if (editAlarm != null) {
            setupEditMode(editAlarm!!)
        } else {
            updateRingtoneName()
            updateMissionHighlight()
        }

        setupListeners()
    }

    private fun initViews() {
        timePicker = findViewById(R.id.time_picker)
        timePicker.setIs24HourView(true)
        labelInput = findViewById(R.id.label_input)
        missionGroup = findViewById(R.id.mission_group)
        saveAlarmButton = findViewById(R.id.save_alarm_button)
        monCheck = findViewById(R.id.mon_check)
        tueCheck = findViewById(R.id.tue_check)
        wedCheck = findViewById(R.id.wed_check)
        thuCheck = findViewById(R.id.thu_check)
        friCheck = findViewById(R.id.fri_check)
        satCheck = findViewById(R.id.sat_check)
        sunCheck = findViewById(R.id.sun_check)
        btnRingtoneSelect = findViewById(R.id.btn_ringtone_select)
        tvRingtoneName = findViewById(R.id.tv_ringtone_name)

        // ★ [추가]
        checkCustomSentence = findViewById(R.id.check_custom_sentence)
    }

    private fun setupEditMode(alarm: Alarm) {
        timePicker.hour = alarm.hour
        timePicker.minute = alarm.minute
        labelInput.setText(alarm.label)
        monCheck.isChecked = alarm.days.contains("월")
        tueCheck.isChecked = alarm.days.contains("화")
        wedCheck.isChecked = alarm.days.contains("수")
        thuCheck.isChecked = alarm.days.contains("목")
        friCheck.isChecked = alarm.days.contains("금")
        satCheck.isChecked = alarm.days.contains("토")
        sunCheck.isChecked = alarm.days.contains("일")

        when(alarm.mission) {
            "수학 문제" -> missionGroup.check(R.id.mission_math)
            "폰 흔들기" -> missionGroup.check(R.id.mission_shake)
            "연타" -> missionGroup.check(R.id.mission_tap)
            "타자 입력" -> {
                missionGroup.check(R.id.mission_typing)
                // ★ [추가] 체크박스 보이기 및 상태 복원
                checkCustomSentence.visibility = View.VISIBLE
                checkCustomSentence.isChecked = alarm.useCustomSentence
            }
            else -> missionGroup.check(R.id.mission_none)
        }
        updateMissionHighlight()

        if (!alarm.ringtoneUri.isNullOrEmpty()) {
            selectedRingtoneUri = Uri.parse(alarm.ringtoneUri)
        }
        updateRingtoneName()
        saveAlarmButton.text = "수정 완료"
    }

    private fun setupListeners() {
        btnRingtoneSelect.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알람 소리 선택")
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri)
            }
            ringtonePickerLauncher.launch(intent)
        }

        // ★ [수정] 미션 선택 시 체크박스 보이기/숨기기
        missionGroup.setOnCheckedChangeListener { _, checkedId ->
            updateMissionHighlight()
            if (checkedId == R.id.mission_typing) {
                checkCustomSentence.visibility = View.VISIBLE
            } else {
                checkCustomSentence.visibility = View.GONE
                checkCustomSentence.isChecked = false
            }
        }

        saveAlarmButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val labelText = labelInput.text.toString().ifBlank { "알람" }
            val selectedDays = buildSelectedDays()
            val missionText = getSelectedMissionLabel()
            val idToSave = editAlarm?.id ?: 0

            val alarmToSave = Alarm(
                id = idToSave,
                hour = hour,
                minute = minute,
                label = labelText,
                days = selectedDays,
                mission = missionText,
                isOn = true,
                ringtoneUri = selectedRingtoneUri?.toString(),
                useCustomSentence = checkCustomSentence.isChecked // ★ [추가] 저장
            )

            if (editAlarm != null) {
                AlarmScheduler.cancel(this, editAlarm!!)
                AlarmRepository.updateAlarm(this, alarmToSave)
            } else {
                AlarmRepository.addAlarm(this, alarmToSave)
            }
            AlarmScheduler.register(this, alarmToSave)
            finish()
        }
        findViewById<View>(R.id.close_button).setOnClickListener { finish() }
    }

    private fun updateRingtoneName() {
        if (selectedRingtoneUri == null) {
            tvRingtoneName.text = "무음"
        } else {
            val ringtone = RingtoneManager.getRingtone(this, selectedRingtoneUri)
            tvRingtoneName.text = ringtone?.getTitle(this) ?: "알 수 없음"
        }
    }

    private fun buildSelectedDays(): List<String> {
        val days = mutableListOf<String>()
        if (monCheck.isChecked) days.add("월")
        if (tueCheck.isChecked) days.add("화")
        if (wedCheck.isChecked) days.add("수")
        if (thuCheck.isChecked) days.add("목")
        if (friCheck.isChecked) days.add("금")
        if (satCheck.isChecked) days.add("토")
        if (sunCheck.isChecked) days.add("일")
        return days
    }

    private fun getSelectedMissionLabel(): String? {
        return when (missionGroup.checkedRadioButtonId) {
            R.id.mission_math -> "수학 문제"
            R.id.mission_shake -> "폰 흔들기"
            R.id.mission_tap -> "연타"
            R.id.mission_typing -> "타자 입력"
            else -> null
        }
    }

    private fun updateMissionHighlight() {
        val missionIds = listOf(R.id.mission_none, R.id.mission_math, R.id.mission_shake, R.id.mission_tap, R.id.mission_typing)
        val selectedId = missionGroup.checkedRadioButtonId
        val selectedBg = R.drawable.bg_mission_option_selected
        val unselectedBg = R.drawable.bg_mission_option_unselected
        val selectedTextColor = ContextCompat.getColor(this, android.R.color.white)
        val unselectedTextColor = ContextCompat.getColor(this, R.color.alarm_label_color)

        missionIds.forEach { id ->
            val button = findViewById<RadioButton?>(id) ?: return@forEach
            if (button.id == selectedId) {
                button.setBackgroundResource(selectedBg)
                button.setTextColor(selectedTextColor)
            } else {
                button.setBackgroundResource(unselectedBg)
                button.setTextColor(unselectedTextColor)
            }
            button.setPadding(40, 24, 40, 24)
        }
    }
}