package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.GeminiClient
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAddDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSubmitPrompt: () -> Unit,
    onScanSampleImage: (Bitmap, String) -> Unit,
    onScanTextFile: (String) -> Unit,
    isApiKeyConfigured: Boolean
) {
    if (!isOpen) return

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    onScanSampleImage(bitmap, "Hình ảnh đã tải lên")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            onScanSampleImage(it, "Hình ảnh từ Camera")
        }
    }

    val textFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val text = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (!text.isNullOrEmpty()) {
                    onScanTextFile(text)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("smart_add_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI helper",
                            tint = Color(0xFFFFB3A0),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Smart Assistant",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section A: Natural Language NLP Input
                Text(
                    text = "Nhập thời gian bằng ngôn ngữ tự nhiên:",
                    fontSize = 14.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    placeholder = {
                        Text(
                            text = "ví dụ: \"Study Java tomorrow at 7pm\" hoặc \"Thiền thư giãn 2h chiều mai\"...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("nlp_input_text"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFB3A0),
                        unfocusedBorderColor = Color(0xFFE0D9D0),
                        focusedContainerColor = CreamBackground,
                        unfocusedContainerColor = CreamBackground,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSubmitPrompt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("nlp_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0)),
                    enabled = inputText.trim().isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Parse",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Phân tích lịch trình AI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color(0xFFECE5D9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Section B: Image OCR Multimodal Scanner & File Import
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = "Scanner",
                        tint = Color(0xFF90A4AE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quét Lịch Trình Tự Động (AI OCR)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                Text(
                    text = "AI sẽ quét nội dung từ camera, ảnh thư viện hoặc tệp văn bản rồi tạo lịch tự động cho bạn xem trước:",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Interactive Buttons for Upload & Capture & Document Parsing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button 1: Tải ảnh lẻ hoặc bảng lớp
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFECE6),
                            contentColor = Color(0xFFFF8A75)
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Chọn ảnh",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tải Ảnh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Button 2: Chụp từ camera
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E3F7),
                            contentColor = Color(0xFF7E57C2)
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Chụp ảnh",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chụp Ảnh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Button 3: Tải tệp văn bản
                    Button(
                        onClick = { textFileLauncher.launch("text/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCFEADF),
                            contentColor = Color(0xFF2E7D32)
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Tải tệp",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tệp Văn Bản", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Header for simulated presets
                Text(
                    text = "Hoặc chọn một mẫu có sẵn dưới đây để thử nghiệm nhanh:",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Horizontal lazy row of awesome preset mock schedule cards
                val samples = listOf(
                    SampleScheduleItem(
                        id = "timetable",
                        title = "Thời khóa biểu lớp k36",
                        description = "Simulated class timetable screenshot",
                        primaryColor = Color(0xFFCFEADF) // Mint
                    ),
                    SampleScheduleItem(
                        id = "workshop",
                        title = "Poster Hội Thảo Self-care",
                        description = "Cozy wellness workshop announcement",
                        primaryColor = Color(0xFFE5E3F7) // Lavender
                    ),
                    SampleScheduleItem(
                        id = "party",
                        title = "Tin nhắn mời tiệc nướng",
                        description = "Party chat invitation notes",
                        primaryColor = Color(0xFFFFFFECE6) // Peach
                    )
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(samples) { sample ->
                        Box(
                            modifier = Modifier
                                .width(135.dp)
                                .height(95.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(sample.primaryColor.copy(alpha = 0.5f))
                                .border(1.5.dp, sample.primaryColor, RoundedCornerShape(16.dp))
                                .clickable {
                                    val generatedBitmap = createSampleScheduleBitmap(sample.title)
                                    onScanSampleImage(generatedBitmap, "Sample OCR scanning: ${sample.title}")
                                }
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "SCAN SAMPLE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = sample.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                if (!isApiKeyConfigured) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF3CD))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "API Info",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Đang chạy chế độ mô phỏng thông minh (chưa cấu hình API Key ở mục Secrets).",
                            fontSize = 10.sp,
                            color = Color(0xFF856404),
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}

data class SampleScheduleItem(
    val id: String,
    val title: String,
    val description: String,
    val primaryColor: Color
)

/**
 * Creates a dynamic Bitmap container dynamically painted to represent realistic schedule text
 * that the Gemini API can scan! (This creates a genuine OCR test bitmap!)
 */
fun createSampleScheduleBitmap(text: String): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // Background color
    canvas.drawColor(AndroidColor.parseColor("#FFFDF9")) // Cozy Cream

    // Draw border
    paint.color = AndroidColor.parseColor("#E5E3F7")
    paint.strokeWidth = 10f
    paint.style = Paint.Style.STROKE
    canvas.drawRect(5f, 5f, 395f, 295f, paint)

    // Title painting
    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.parseColor("#5D4037")
    paint.textSize = 24f
    paint.isAntiAlias = true
    canvas.drawText("COZY CALENDAR SCHEDULE", 40f, 60f, paint)

    paint.textSize = 14f
    paint.color = AndroidColor.parseColor("#8C877E")
    canvas.drawText("Preset Asset: $text", 40f, 90f, paint)

    // Mock contents that Gemini can OCR
    paint.color = AndroidColor.parseColor("#3D3A36")
    paint.textSize = 16f
    if (text.contains("Thời khoa") || text.contains("Timetable") || text.contains("lớp")) {
        canvas.drawText("- Thu 2 (Monday): 9:00 AM - Mobile Dev Class, Room 402", 40f, 150f, paint)
        canvas.drawText("- Thu 3 (Tuesday): 2:30 PM - Cozy Reading session", 40f, 190f, paint)
        canvas.drawText("- Happy self-care with Mochi pet cat", 40f, 230f, paint)
    } else if (text.contains("Workshop") || text.contains("Self-care") || text.contains("Hội thảo")) {
        canvas.drawText("- Chu nhat (Sunday) at 3:00 PM next day", 40f, 150f, paint)
        canvas.drawText("  Event: Wellness & Yoga retreat session", 40f, 180f, paint)
        canvas.drawText("  Cozy environment workshop for deep meditation", 40f, 210f, paint)
    } else {
        canvas.drawText("- BBQ Party with close friends", 40f, 150f, paint)
        canvas.drawText("- Sunday 10:00 AM at Golden Tea House", 40f, 190f, paint)
    }

    return bitmap
}
