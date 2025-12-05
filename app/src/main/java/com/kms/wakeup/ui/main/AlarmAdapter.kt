package com.kms.wakeup.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.kms.wakeup.R
import com.kms.wakeup.data.model.Alarm

// ★ 생성자에 onItemClick 람다 추가
class AlarmAdapter(
    private val items: MutableList<Alarm>,
    private val onItemClick: (Alarm) -> Unit,        // 클릭 시 실행할 동작
    private val onSwitchClick: (Alarm, Boolean) -> Unit, // 스위치 동작
    private val onDeleteClick: (Alarm) -> Unit       // 삭제 동작
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView as MaterialCardView
        val timeText: TextView = itemView.findViewById(R.id.alarm_time_text)
        val labelText: TextView = itemView.findViewById(R.id.alarm_label_text)
        val missionChip: TextView = itemView.findViewById(R.id.mission_chip)
        val switchView: SwitchCompat = itemView.findViewById(R.id.alarm_switch)
        val deleteIcon: ImageView = itemView.findViewById(R.id.delete_icon)
        val dayContainer: LinearLayout = itemView.findViewById(R.id.day_container)

        init {
            // 카드 전체 클릭 리스너 설정
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = items[position]

        holder.timeText.text = String.format("%02d:%02d", alarm.hour, alarm.minute)
        holder.labelText.text = alarm.label.ifBlank { "알람" }

        // 미션 칩 설정
        if (alarm.mission.isNullOrBlank() || alarm.mission == "미션 없음") {
            holder.missionChip.visibility = View.GONE
        } else {
            holder.missionChip.visibility = View.VISIBLE
            holder.missionChip.text = alarm.mission
            // (색상 설정 로직은 HomeFragment나 여기서 처리 가능 - 여기서는 간단히 텍스트만)
        }

        // 요일 칩 설정
        holder.dayContainer.removeAllViews()
        for (d in alarm.days) {
            val chip = TextView(holder.itemView.context).apply {
                text = d
                textSize = 12f
                setPadding(24, 12, 24, 12)
                background = ContextCompat.getDrawable(context, R.drawable.bg_day_chip_small)
                setTextColor(ContextCompat.getColor(context, R.color.day_chip_text_on))
            }
            val lp = LinearLayout.LayoutParams(-2, -2).apply { marginEnd = 12 }
            chip.layoutParams = lp
            holder.dayContainer.addView(chip)
        }

        holder.switchView.setOnCheckedChangeListener(null) // 리스너 초기화 중요
        holder.switchView.isChecked = alarm.isOn
        holder.card.alpha = if (alarm.isOn) 1.0f else 0.5f

        holder.switchView.setOnCheckedChangeListener { _, isChecked ->
            alarm.isOn = isChecked
            holder.card.alpha = if (isChecked) 1.0f else 0.5f
            onSwitchClick(alarm, isChecked)
        }

        holder.deleteIcon.setOnClickListener {
            onDeleteClick(alarm)
        }
    }

    fun updateData(newItems: List<Alarm>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}