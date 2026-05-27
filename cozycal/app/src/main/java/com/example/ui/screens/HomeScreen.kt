package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.Habit
import com.example.data.model.PetState
import com.example.ui.components.PetRoomCanvas
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.safeParseColor
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habitsList.collectAsState()
    val events by viewModel.eventsList.collectAsState()
    val petState by viewModel.petState.collectAsState()

    val todayStr = SimpleDateFormat("EEEE, dd MMMM", Locale("vi", "VN")).format(Date())

    // Filter events for today dynamically based on system calendar date limits
    val todayEvents = remember(events) {
        val calStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val calEnd = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }
        val startMillis = calStart.timeInMillis
        val endMillis = calEnd.timeInMillis
        
        events.filter { event ->
            event.startTime in startMillis..endMillis || 
            event.endTime in startMillis..endMillis || 
            (event.startTime <= startMillis && event.endTime >= endMillis)
        }.sortedBy { it.startTime }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_column")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Greeting & Date
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
                val greetingText = when (hour) {
                    in 5..11 -> "Chào buổi sáng"
                    in 12..17 -> "Chào buổi chiều"
                    in 18..22 -> "Chào buổi tối"
                    else -> "Chào cú đêm"
                }
                val userNameToDisplay = petState?.userName?.trim()?.ifEmpty { "bạn" } ?: "bạn"
                Text(
                    text = "$greetingText, $userNameToDisplay ✨",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = todayStr,
                    fontSize = 14.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 2. Pet Status Bar Grid Summary
        item {
            petState?.let { pet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pet_status_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tiny pet canvas preview widget
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CreamBackground)
                        ) {
                            PetRoomCanvas(
                                isSleeping = viewModel.isFocusTimerRunning,
                                activeAccessories = pet.activeAccessories.split(",").toSet(),
                                petType = pet.petType
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Stats side
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🐱 ${pet.name}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Lv. ${pet.level}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB3A0)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Mood Indicator
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Mood",
                                    tint = Color(0xFFFF8A75),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                LinearProgressIndicator(
                                    progress = pet.mood / 100f,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFFFFB3A0),
                                    trackColor = Color(0xFFFFECE6)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${pet.mood}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Coin balance widget
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF7CE))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = Color(0xFFFBC02D),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${pet.coins} Xu",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. AI Suggestions & Quick Smart Add Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showQuickAddDialog = true }
                    .testTag("smart_add_trigger"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECE6)),
                border = BorderStroke(1.dp, Color(0xFFFFB3A0).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Quick Add",
                            tint = Color(0xFFFFB3A0)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Lập Lịch Bằng AI",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Viết kế hoạch hoặc Quét ảnh lịch trình...",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color(0xFFFFB3A0)
                    )
                }
            }
        }

        // 4. Habits List Widget
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habits Thư Giãn",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Hàng Ngày",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (habits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CreamSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có habit nào hôm nay. Hãy tạo 1 thói quen 🌱",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(habits) { habit ->
                val todayStrFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val isCompletedToday = habit.lastCompletedDate == todayStrFormat

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_card_${habit.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompletedToday) Color(0xFFCFEADF) else CreamSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.icon,
                                fontSize = 22.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = habit.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = "Chuỗi: ${habit.streak} ngày liên tiếp 🔥",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.completeHabit(habit) },
                            modifier = Modifier.testTag("complete_habit_${habit.id}")
                        ) {
                            Icon(
                                imageVector = if (isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (isCompletedToday) "Completed" else "Incomplete",
                                tint = if (isCompletedToday) Color(0xFF388E3C) else Color(0xFFFFB3A0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Today's Timelines (Agenda)
        item {
            Text(
                text = "Lịch Trình Hôm Nay",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        if (todayEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CreamSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có sự kiện nào hôm nay. Hãy tận hưởng kỳ nghỉ! 😴☕",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(todayEvents) { event ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val sFormatted = timeFormat.format(Date(event.startTime))
                val eFormatted = timeFormat.format(Date(event.endTime))
                val eventColor = safeParseColor(event.colorHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, eventColor, RoundedCornerShape(18.dp))
                        .testTag("event_card_${event.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored time tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(eventColor)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = sFormatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "đến $eFormatted",
                                    fontSize = 8.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textDecoration = if (event.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                            if (event.description.isNotEmpty()) {
                                Text(
                                    text = event.description,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.completeEvent(event) },
                            modifier = Modifier.testTag("complete_event_${event.id}")
                        ) {
                            Icon(
                                imageVector = if (event.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (event.isCompleted) "Completed" else "Incomplete",
                                tint = if (event.isCompleted) Color(0xFF388E3C) else Color(0xFFFFB3A0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
