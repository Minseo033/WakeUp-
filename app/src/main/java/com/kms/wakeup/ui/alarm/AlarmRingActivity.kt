package com.kms.wakeup.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kms.wakeup.R
import com.kms.wakeup.alarm.AlarmScheduler
import com.kms.wakeup.data.AlarmRepository
import com.kms.wakeup.data.model.Alarm
import com.kms.wakeup.data.model.AlarmHistory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

class AlarmRingActivity : AppCompatActivity(), SensorEventListener {

    private var ringtone: Ringtone? = null

    private lateinit var ringTimeText: TextView
    private lateinit var dateText: TextView
    private lateinit var ringMessageText: TextView

    private lateinit var simpleContainer: View
    private lateinit var btnSimpleStop: Button
    private lateinit var btnSimpleSnooze: Button

    private lateinit var mathContainer: View
    private lateinit var mathQuestionText: TextView
    private lateinit var mathInput: EditText
    private lateinit var btnSubmitMath: Button
    private var correctAnswer: Int = 0

    private lateinit var shakeContainer: View
    private lateinit var shakeMissionDesc: TextView
    private lateinit var shakeCountText: TextView
    private lateinit var shakeProgressBar: ProgressBar

    private lateinit var tapContainer: View
    private lateinit var tapMissionDesc: TextView
    private lateinit var tapCountText: TextView
    private lateinit var tapProgressBar: ProgressBar
    private lateinit var btnTap: View

    private lateinit var typingContainer: View
    private lateinit var typingTargetText: TextView
    private lateinit var typingInput: EditText
    private lateinit var typingProgressBar: ProgressBar
    private lateinit var typingProgressText: TextView
    private var currentTypingTarget = ""

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var currentShakeCount = 0
    private var targetShakeCount = 30
    private val SHAKE_THRESHOLD = 1.3f
    private val SHAKE_WAIT_TIME_MS = 300
    private var lastShakeTime: Long = 0

    private var currentTapCount = 0
    private var targetTapCount = 50

    private var difficulty = "normal"
    private var difficultyMultiplier = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        turnScreenOnAndKeyguard()
        setContentView(R.layout.activity_alarm_ring)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        initViews()

        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)
        val mission = intent.getStringExtra("mission")

        ringTimeText.text = String.format("%02d:%02d", hour, minute)
        setDateText()
        ringMessageText.text = "알람이 울리고 있습니다"

        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        difficulty = prefs.getString("mission_difficulty", "normal") ?: "normal"

        difficultyMultiplier = when(difficulty) {
            "easy" -> 0.5f
            "normal" -> 1.0f
            else -> 2.0f
        }

        startRingtone()

        when (mission) {
            "수학 문제" -> showMathMission()
            "폰 흔들기" -> showShakeMission()
            "연타" -> showTapMission()
            "타자 입력" -> showTypingMission()
            else -> showSimpleScreen()
        }

        setupListeners()
    }

    private fun turnScreenOnAndKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun initViews() {
        ringTimeText = findViewById(R.id.ring_time)
        dateText = findViewById(R.id.date_text)
        ringMessageText = findViewById(R.id.ring_message)

        simpleContainer = findViewById(R.id.simple_alarm_container)
        btnSimpleStop = findViewById(R.id.btn_simple_stop)
        btnSimpleSnooze = findViewById(R.id.btn_simple_snooze)

        mathContainer = findViewById(R.id.math_alarm_container)
        mathQuestionText = findViewById(R.id.math_question)
        mathInput = findViewById(R.id.math_input)
        btnSubmitMath = findViewById(R.id.btn_submit_math)

        shakeContainer = findViewById(R.id.shake_alarm_container)
        shakeMissionDesc = findViewById(R.id.shake_mission_desc)
        shakeCountText = findViewById(R.id.shake_count_text)
        shakeProgressBar = findViewById(R.id.shake_progress_bar)

        tapContainer = findViewById(R.id.tap_alarm_container)
        tapMissionDesc = findViewById(R.id.tap_mission_desc)
        tapCountText = findViewById(R.id.tap_count_text)
        tapProgressBar = findViewById(R.id.tap_progress_bar)
        btnTap = findViewById(R.id.btn_tap_mission)

        typingContainer = findViewById(R.id.typing_alarm_container)
        typingTargetText = findViewById(R.id.typing_target_text)
        typingInput = findViewById(R.id.typing_input)
        typingProgressBar = findViewById(R.id.typing_progress_bar)
        typingProgressText = findViewById(R.id.typing_progress_text)
    }

    private fun setDateText() {
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN)
        dateText.text = dateFormat.format(now.time)
    }

    private fun startRingtone() {
        try {
            val uriString = intent.getStringExtra("ringtoneUri")
            val uri = if (!uriString.isNullOrEmpty()) {
                Uri.parse(uriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            ringtone = RingtoneManager.getRingtone(this, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        btnSimpleStop.setOnClickListener { stopAlarmAndFinish() }

        btnSimpleSnooze.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 5)
            val snoozeAlarm = Alarm(
                id = Random.nextInt(100000, 999999),
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
                label = "다시 알림",
                days = emptyList(),
                mission = intent.getStringExtra("mission"),
                isOn = true,
                ringtoneUri = intent.getStringExtra("ringtoneUri"),
                useCustomSentence = intent.getBooleanExtra("useCustomSentence", false)
            )

            AlarmScheduler.register(this, snoozeAlarm)
            Toast.makeText(this, "5분 뒤에 다시 깨워드릴게요! 💤", Toast.LENGTH_SHORT).show()

            ringtone?.stop()
            ringtone = null
            unregisterSensor()
            finish()
        }

        btnSubmitMath.setOnClickListener { checkMathAnswer() }
        btnTap.setOnClickListener { handleTap() }

        typingInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkTypingMatch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun hideAllContainers() {
        simpleContainer.visibility = View.GONE
        mathContainer.visibility = View.GONE
        shakeContainer.visibility = View.GONE
        tapContainer.visibility = View.GONE
        typingContainer.visibility = View.GONE
    }

    private fun showSimpleScreen() {
        hideAllContainers()
        simpleContainer.visibility = View.VISIBLE
    }

    private fun showMathMission() {
        hideAllContainers()
        mathContainer.visibility = View.VISIBLE
        generateMathQuestion()
    }

    private fun showShakeMission() {
        hideAllContainers()
        shakeContainer.visibility = View.VISIBLE

        targetShakeCount = when(difficulty) {
            "easy" -> Random.nextInt(20, 41)
            "normal" -> Random.nextInt(50, 71)
            else -> Random.nextInt(100, 141)
        }
        currentShakeCount = 0

        shakeMissionDesc.text = "화면을 ${targetShakeCount}번 흔드세요"
        updateShakeUI()
        registerSensor()
    }

    private fun showTapMission() {
        hideAllContainers()
        tapContainer.visibility = View.VISIBLE

        targetTapCount = when(difficulty) {
            "easy" -> Random.nextInt(40, 61)
            "normal" -> Random.nextInt(80, 121)
            else -> Random.nextInt(160, 241)
        }
        currentTapCount = 0

        tapMissionDesc.text = "버튼을 ${targetTapCount}번 터치하세요"
        updateTapUI()
    }

    private fun showTypingMission() {
        hideAllContainers()
        typingContainer.visibility = View.VISIBLE

        // ★ [수정됨] 커스텀 문장 사용 여부 확인
        val useCustom = intent.getBooleanExtra("useCustomSentence", false)

        if (useCustom) {
            val customList = AlarmRepository.getCustomSentences(this)
            if (customList.isNotEmpty()) {
                currentTypingTarget = customList.random().text
            } else {
                currentTypingTarget = "저장된 나만의 문장이 없습니다. 설정에서 추가해주세요!"
            }
        } else {
            val sentences = listOf(
                "성공은 매일 반복되는 작은 노력들의 합이다",
                "오늘 걷지 않으면 내일은 뛰어야 한다",
                "피할 수 없으면 즐겨라",
                "나의 미래는 오늘 내가 무엇을 하느냐에 달려있다",
                "꿈을 꾸기에 인생은 너무나 아름답다",
                "시작이 반이다 용기를 내어 시작하라",
                "오늘 당신에게 좋은 일이 눈사태처럼 일어납니다",
                "긍정적인 마음은 어떤 난관도 돌파하는 힘이 된다",
                "나는 날마다 모든 면에서 점점 더 좋아지고 있다",
                "웃음은 가장 적은 비용으로 투자를 하는 것이다",
                "당신의 하루가 별보다 더 빛나길 응원합니다",
                "행복해서 웃는 게 아니라 웃어서 행복한 것이다",
                "건강한 신체에 건전한 정신이 깃든다",
                "아침 스트레칭은 하루를 바꾸는 기적이다",
                "물 한 잔으로 몸과 마음을 상쾌하게 깨우세요",
                "늦었다고 생각할 때가 가장 빠를 때다",
                "중요한 것은 꺾이지 않는 마음이다",
                "실패는 성공을 위한 연습일 뿐이다",
                "당신은 사랑받기 위해 태어난 사람입니다",
                "기회는 준비된 자에게만 찾아온다",
                "오늘 흘린 땀은 내일의 기쁨이 된다",
                "어제보다 더 나은 오늘을 만들자",
                "나 자신을 믿는 것이 성공의 제1비결이다",
                "고통이 없으면 얻는 것도 없다",
                "생각하는 대로 살지 않으면 사는 대로 생각하게 된다",
                "인생은 속도가 아니라 방향이다",
                "가장 큰 위험은 위험 없는 삶을 사는 것이다",
                "오늘 하루도 나에게 주어진 선물입니다",
                "지금 잠을 자면 꿈을 꾸지만 지금 깨면 꿈을 이룬다",
                "당신의 잠재력은 당신의 상상보다 훨씬 큽니다"
            )
            currentTypingTarget = sentences.random()
        }

        typingTargetText.text = currentTypingTarget
        typingInput.setText("")
        typingProgressBar.progress = 0
        typingProgressText.text = "0%"
    }

    private fun generateMathQuestion() {
        var a: Int; var b: Int; var c: Int; var op: String

        when (difficulty) {
            "easy" -> {
                a = Random.nextInt(2, 10)
                b = Random.nextInt(2, 10)
                op = "+"
                mathQuestionText.text = "$a + $b ="
                correctAnswer = a + b
            }
            "normal" -> {
                val type = Random.nextInt(0, 5)
                if (type >= 2) {
                    a = Random.nextInt(2, 10)
                    b = Random.nextInt(2, 10)
                    op = "×"
                    mathQuestionText.text = "$a × $b ="
                    correctAnswer = a * b
                } else {
                    a = Random.nextInt(10, 50)
                    b = Random.nextInt(10, 50)
                    op = if (type == 0) "+" else "-"
                    if (op == "+") {
                        mathQuestionText.text = "$a + $b ="
                        correctAnswer = a + b
                    } else {
                        val maxVal = if(a>b) a else b
                        val minVal = if(a>b) b else a
                        mathQuestionText.text = "$maxVal - $minVal ="
                        correctAnswer = maxVal - minVal
                    }
                }
            }
            else -> {
                a = Random.nextInt(10, 20)
                b = Random.nextInt(2, 10)
                c = Random.nextInt(1, 10)
                mathQuestionText.text = "$a × $b + $c ="
                correctAnswer = (a * b) + c
            }
        }
    }

    private fun checkMathAnswer() {
        val input = mathInput.text.toString().trim()
        if (input.isEmpty()) return
        val userAnswer = input.toIntOrNull()
        if (userAnswer == correctAnswer) stopAlarmAndFinish()
        else { Toast.makeText(this, "오답입니다.", Toast.LENGTH_SHORT).show(); mathInput.setText("") }
    }

    private fun registerSensor() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }
    private fun unregisterSensor() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val curTime = System.currentTimeMillis()
            if ((curTime - lastShakeTime) > SHAKE_WAIT_TIME_MS) {
                val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
                val gForce = sqrt((x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))
                if (gForce > SHAKE_THRESHOLD) {
                    lastShakeTime = curTime
                    currentShakeCount++
                    updateShakeUI()
                    if (currentShakeCount >= targetShakeCount) stopAlarmAndFinish()
                }
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateShakeUI() {
        val remaining = targetShakeCount - currentShakeCount
        shakeCountText.text = if (remaining > 0) remaining.toString() else "0"
        shakeProgressBar.max = targetShakeCount
        shakeProgressBar.progress = currentShakeCount
    }

    private fun handleTap() {
        currentTapCount++
        updateTapUI()
        if (currentTapCount >= targetTapCount) stopAlarmAndFinish()
    }

    private fun updateTapUI() {
        val remaining = targetTapCount - currentTapCount
        tapCountText.text = if (remaining > 0) remaining.toString() else "0"
        tapProgressBar.max = targetTapCount
        tapProgressBar.progress = currentTapCount
    }

    private fun checkTypingMatch(input: String) {
        val target = currentTypingTarget
        var matchCount = 0
        val length = minOf(input.length, target.length)
        for (i in 0 until length) {
            if (input[i] == target[i]) matchCount++
            else break
        }
        val progress = (matchCount.toFloat() / target.length.toFloat() * 100).toInt()
        typingProgressBar.progress = progress
        typingProgressText.text = "$progress%"

        if (input == target) {
            Toast.makeText(this, "미션 성공! 알람이 종료됩니다.", Toast.LENGTH_SHORT).show()
            stopAlarmAndFinish()
        }
    }

    private fun stopAlarmAndFinish() {
        ringtone?.stop()
        ringtone = null
        unregisterSensor()
        saveWakeUpHistory()
        finish()
    }

    private fun saveWakeUpHistory() {
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(now.time)
        val timeStr = timeFormat.format(now.time)
        val missionName = intent.getStringExtra("mission") ?: "기본 알람"

        val history = AlarmHistory(
            timestamp = System.currentTimeMillis(),
            date = dateStr,
            time = timeStr,
            missionType = missionName
        )
        try {
            AlarmRepository.addHistory(this, history)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
        unregisterSensor()
    }
    override fun onPause() {
        super.onPause()
        unregisterSensor()
    }
    override fun onResume() {
        super.onResume()
        if (shakeContainer.visibility == View.VISIBLE && currentShakeCount < targetShakeCount) {
            registerSensor()
        }
    }
}