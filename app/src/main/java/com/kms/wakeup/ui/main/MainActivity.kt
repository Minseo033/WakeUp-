package com.kms.wakeup.ui.main

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kms.wakeup.R
import com.kms.wakeup.ui.analysis.AnalysisFragment
import com.kms.wakeup.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var mainLayout: ConstraintLayout

    // 알림 권한 요청 결과 처리 (Android 13+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "알림 권한이 없으면 알람이 울리지 않을 수 있습니다.", Toast.LENGTH_LONG).show()
            }
            // 알림 권한 처리 후 -> 다른 앱 위에 그리기 권한 체크로 넘어감
            checkOverlayPermission()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)
        mainLayout = findViewById(R.id.main)

        // ★ [화면 잘림 방지] 시스템 바(상태바, 내비게이션바)만큼 안쪽으로 패딩을 줍니다.
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop, // 상태바 높이만큼 내림
                view.paddingRight,
                systemBarsInsets.bottom // 내비게이션 바 높이만큼 올림
            )
            WindowInsetsCompat.CONSUMED
        }

        // 초기 화면 설정
        if (savedInstanceState == null) {
            openFragment(HomeFragment())
        }

        // 하단 탭 리스너
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> openFragment(HomeFragment())
                R.id.menu_analysis -> openFragment(AnalysisFragment())
                R.id.menu_settings -> openFragment(SettingsFragment())
            }
            true
        }

        // ★ 앱 켜자마자 필수 권한 체크 및 요청
        checkAllPermissions()
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, fragment)
            .commit()
    }

    // ================= [권한 체크 로직] =================

    private fun checkAllPermissions() {
        // 1. 알림 권한 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return // 권한 요청 팝업이 뜨므로 여기서 중단 (결과는 콜백으로 감)
            }
        }

        // 2. 이미 알림 권한이 있거나 안드로이드 13 미만이면 바로 오버레이 체크
        checkOverlayPermission()
    }

    private fun checkOverlayPermission() {
        // 다른 앱 위에 그리기 권한 (Android 10 이상 필수)
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("권한 필요")
                .setMessage("알람이 울릴 때 화면을 즉시 띄우려면 '다른 앱 위에 표시' 권한이 필요합니다.")
                .setPositiveButton("설정하러 가기") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("취소", null)
                .show()
        } else {
            // 오버레이 권한도 있으면 -> 정확한 알람 권한 체크 (마지막)
            checkExactAlarmPermission()
        }
    }

    private fun checkExactAlarmPermission() {
        // 정확한 알람 권한 (Android 12 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "정확한 시간에 알람을 울리기 위해 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }
}