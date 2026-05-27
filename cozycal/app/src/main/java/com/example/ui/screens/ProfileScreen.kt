package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val focusSessions by viewModel.focusSessions.collectAsState()
    val events by viewModel.eventsList.collectAsState()
    val habits by viewModel.habitsList.collectAsState()
    val petState by viewModel.petState.collectAsState()

    val totalCompletedEvents = remember(events) { events.count { it.isCompleted } }
    val totalFocusMinutes = remember(focusSessions) { focusSessions.sumOf { it.durationMinutes } }
    val totalFocusSessions = focusSessions.size

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInputName by remember { mutableStateOf(petState?.name ?: "Mochi") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_column")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val avatarEmoji = when (petState?.petType ?: "Cat") {
                        "Cat" -> "🐱"
                        "Bunny" -> "🐰"
                        "Fox" -> "🦊"
                        "Bear" -> "🐻"
                        "Penguin" -> "🐧"
                        "Hamster" -> "🐹"
                        else -> "🐱"
                    }
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFECE6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatarEmoji, fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = petState?.userName ?: "Quốc Toàn",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Đồng hành: ${petState?.name ?: "Mochi"} (${petState?.petType ?: "Mèo"}) • ${petState?.petPersonality ?: "Thư thái"}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (petState?.isCalendarConnected == true) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Đã kết nối Google Calendar 🗓️✨", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Metrics Statistics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    number = "$totalCompletedEvents",
                    label = "Sự kiện Đã Xong",
                    color = Color(0xFFCFEADF) // Mint
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    number = "${totalFocusMinutes}p",
                    label = "Thời Gian Tập Trung",
                    color = Color(0xFFE5E3F7) // Lavender
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    number = "$totalFocusSessions",
                    label = "Số Phiên Focus",
                    color = Color(0xFFD6EDF8) // Blue
                )
            }
        }

        // 3. Focus History List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Stats",
                    tint = Color(0xFFFFB3A0),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lịch Sử Tập Trung Sức Khỏe",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }

        if (focusSessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chưa có phiên tập trung nào. Cùng gấu nâu/mochi học tập để mở khóa nhé! 🌱",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(focusSessions.take(3)) { session ->
                val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
                val formattedDate = remember(session.timestamp) { formatter.format(Date(session.timestamp)) }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("focus_session_item_${session.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = "Focus",
                                tint = Color(0xFFFFB3A0)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = session.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = formattedDate,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = "+${session.durationMinutes} phút",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF388E3C)
                        )
                    }
                }
            }
        }

        // 4. Integrations & Administration Options Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_sync_tip"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Sync",
                            tint = Color(0xFFFFB3A0)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tùy Chọn & Đồng Bộ Hỏa Tốc",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    Text(
                        text = "Quản lý thiết lập ngôi nhà chung CozyCal cùng thú cưng cực đơn giản của bạn.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                renameInputName = petState?.name ?: "Mochi"
                                showRenameDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFECE6))
                        ) {
                            Text("Đổi Tên Sủng Vật", color = Color(0xFFFF8A75), fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.performCalendarSyncSimulation() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E3F7))
                        ) {
                            Text("Đồng Bộ Lịch 🔄", color = Color(0xFF9575CD), fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.logoutUser() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECEFF1))
                    ) {
                        Text("Đăng xuất khỏi CozyCal 👋", color = TextDark, fontSize = 11.sp)
                    }

                    TextButton(
                        onClick = { viewModel.resetAllData() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Cài lại dữ liệu hệ thống (Reset) ⚠️", color = Color.Red.copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Đổi Tên Bé Đồng Hành", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInputName,
                    onValueChange = { renameInputName = it },
                    label = { Text("Tên sủng vật") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renamePet(renameInputName)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                ) {
                    Text("Lưu Tên 😼")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Hủy", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}
