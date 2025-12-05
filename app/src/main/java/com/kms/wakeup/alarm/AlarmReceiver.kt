package com.kms.wakeup.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kms.wakeup.ui.alarm.AlarmRingActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)
        val label = intent.getStringExtra("label")
        val mission = intent.getStringExtra("mission")
        val ringtoneUri = intent.getStringExtra("ringtoneUri")
        val useCustomSentence = intent.getBooleanExtra("useCustomSentence", false) // ★ [추가]

        val ringIntent = Intent(context, AlarmRingActivity::class.java).apply {
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("label", label)
            putExtra("mission", mission)
            putExtra("ringtoneUri", ringtoneUri)
            putExtra("useCustomSentence", useCustomSentence) // ★ [추가]
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        context.startActivity(ringIntent)
    }
}