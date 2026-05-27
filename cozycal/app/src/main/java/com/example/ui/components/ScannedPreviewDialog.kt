package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CalendarEvent
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.safeParseColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannedPreviewDialog(
    isOpen: Boolean,
    events: List<CalendarEvent>,
    onDismiss: () -> Unit,
    onConfirm: (List<CalendarEvent>) -> Unit
) {
    if (!isOpen) return

    var editableEvents by remember(events) { mutableStateOf(events) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("scanned_preview_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Scan",
                        tint = Color(0xFFFFB3A0),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xem Trước & Sửa Lịch AI ✍️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Text(
                    text = "Bấm biểu tượng sửa để điều chỉnh thông tin, danh mục, mốc thời gian hoặc xóa những sự kiện chưa đúng:",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    if (editableEvents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Lịch trống... Hãy tải lên ảnh hoặc tệp tin khác nhé! 🌱",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(editableEvents) { index, ev ->
                                val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val color = safeParseColor(ev.colorHex)

                                if (editingIndex == index) {
                                    // Editable form row
                                    var editedTitle by remember { mutableStateOf(ev.title) }
                                    var editedDesc by remember { mutableStateOf(ev.description) }
                                    var editedStartTimeStr by remember { mutableStateOf(sdfTime.format(Date(ev.startTime))) }
                                    var editedEndTimeStr by remember { mutableStateOf(sdfTime.format(Date(ev.endTime))) }
                                    var editedCategory by remember { mutableStateOf(ev.category) }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(color.copy(alpha = 0.2f))
                                            .border(1.5.dp, color, RoundedCornerShape(14.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Đang Sửa Sự Kiện #${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Title edit
                                        OutlinedTextField(
                                            value = editedTitle,
                                            onValueChange = { editedTitle = it },
                                            label = { Text("Tựa đề", fontSize = 11.sp) },
                                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Description edit
                                        OutlinedTextField(
                                            value = editedDesc,
                                            onValueChange = { editedDesc = it },
                                            label = { Text("Ghi chú / Địa điểm", fontSize = 11.sp) },
                                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Times edit
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editedStartTimeStr,
                                                onValueChange = { editedStartTimeStr = it },
                                                label = { Text("Bắt đầu (HH:mm)", fontSize = 10.sp) },
                                                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = editedEndTimeStr,
                                                onValueChange = { editedEndTimeStr = it },
                                                label = { Text("Kết thúc (HH:mm)", fontSize = 10.sp) },
                                                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Category Chips
                                        Text("Danh mục:", fontSize = 11.sp, color = TextMuted)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf(
                                                "study" to "Học",
                                                "self-care" to "Healing",
                                                "work" to "Việc",
                                                "routine" to "Thói quen",
                                                "general" to "Chung"
                                            ).forEach { (catKey, catName) ->
                                                val isSel = editedCategory == catKey
                                                val chipColor = when (catKey) {
                                                    "study" -> Color(0xFFCFEADF)
                                                    "self-care" -> Color(0xFFE5E3F7)
                                                    "work" -> Color(0xFFD6EDF8)
                                                    "routine" -> Color(0xFFFFF7CE)
                                                    else -> Color(0xFFFFECE6)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSel) chipColor else Color.Transparent)
                                                        .border(1.dp, if (isSel) Color.DarkGray else Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                        .clickable { editedCategory = catKey }
                                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = catName,
                                                        fontSize = 10.sp,
                                                        color = if (isSel) Color.Black else TextDark,
                                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Save/Done button row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Button(
                                                onClick = {
                                                    // Parse updated start time & end time safely
                                                    val finalStart = updateTimeEpochMillis(ev.startTime, editedStartTimeStr)
                                                    val finalEnd = updateTimeEpochMillis(ev.endTime, editedEndTimeStr)
                                                    val catColor = when (editedCategory) {
                                                        "study" -> "#CFEADF"
                                                        "self-care" -> "#E5E3F7"
                                                        "work" -> "#D6EDF8"
                                                        "routine" -> "#FFF7CE"
                                                        else -> "#FFFFECE6"
                                                    }

                                                    val updatedItem = ev.copy(
                                                        title = editedTitle,
                                                        description = editedDesc,
                                                        startTime = finalStart,
                                                        endTime = finalEnd,
                                                        category = editedCategory,
                                                        colorHex = catColor
                                                    )

                                                    val updatedList = editableEvents.toMutableList()
                                                    updatedList[index] = updatedItem
                                                    editableEvents = updatedList
                                                    editingIndex = null
                                                },
                                                modifier = Modifier.height(34.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Save edit",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Xong", fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                } else {
                                    // Regular state row
                                    val sStr = sdfTime.format(Date(ev.startTime))
                                    val eStr = sdfTime.format(Date(ev.endTime))
                                    val dateStr = sdfDate.format(Date(ev.startTime))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(color.copy(alpha = 0.4f))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ev.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark
                                            )
                                            Text(
                                                text = "Thời gian: $sStr - $eStr Ngày $dateStr",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                color = TextMuted
                                            )
                                            if (ev.description.isNotEmpty()) {
                                                Text(
                                                    text = ev.description,
                                                    fontSize = 9.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                        // Edit/Delete options
                                        Row {
                                            IconButton(
                                                onClick = { editingIndex = index },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Sửa sự kiện",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    val updatedList = editableEvents.toMutableList()
                                                    updatedList.removeAt(index)
                                                    editableEvents = updatedList
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Xóa sự kiện",
                                                    tint = Color(0xFFC62828),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hủy", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onConfirm(editableEvents) },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_import_scan_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0)),
                        enabled = editableEvents.isNotEmpty()
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm Vào Lịch", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Safely parse or adjust times in Epoch millis based on entered time string "HH:mm"
 */
private fun updateTimeEpochMillis(originalTimeMillis: Long, timeString: String): Long {
    return try {
        val date = Date(originalTimeMillis)
        val cal = java.util.Calendar.getInstance().apply { time = date }
        val parts = timeString.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].trim().toIntOrNull() ?: cal.get(java.util.Calendar.HOUR_OF_DAY)
            val min = parts[1].trim().toIntOrNull() ?: cal.get(java.util.Calendar.MINUTE)
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
            cal.set(java.util.Calendar.MINUTE, min)
        }
        cal.timeInMillis
    } catch (e: Exception) {
        originalTimeMillis
    }
}
