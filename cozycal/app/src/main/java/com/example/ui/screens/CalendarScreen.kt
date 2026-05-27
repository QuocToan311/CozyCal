package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.ui.theme.*
import com.example.ui.theme.safeParseColor
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.eventsList.collectAsState()

    var showManualAddForm by remember { mutableStateOf(false) }
    var currentCalendarView by remember { mutableStateOf("day") } // "day", "month", "agenda"

    val monthCalendarDays = remember(viewModel.selectedDateInMillis) {
        val list = mutableListOf<Date?>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = viewModel.selectedDateInMillis
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val month = cal.get(Calendar.MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val paddingCount = firstDayOfWeek - 1
        repeat(paddingCount) {
            list.add(null)
        }
        while (cal.get(Calendar.MONTH) == month) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Manual Event Add Form Fields
    var manualTitle by remember { mutableStateOf("") }
    var manualDesc by remember { mutableStateOf("") }
    var manualStartHour by remember { mutableStateOf(9) }
    var manualStartMin by remember { mutableStateOf(0) }
    var manualDuration by remember { mutableStateOf(60) }
    var manualCategory by remember { mutableStateOf("general") }
    var manualOffsetDays by remember { mutableStateOf(0) } // 0 = today, 1 = tomorrow

    // Prepare horizontal week days array (next 7 days starting today)
    val weekDays = remember {
        val daysList = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        repeat(7) {
            daysList.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        daysList
    }

    val selectedFormatted = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi", "VN"))
        .format(Date(viewModel.selectedDateInMillis))

    // Match events for selected day (using SimpleDateFormat to compare days)
    val filteredEvents = remember(events, viewModel.selectedDateInMillis) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDayStr = sdf.format(Date(viewModel.selectedDateInMillis))
        events.filter {
            sdf.format(Date(it.startTime)) == selectedDayStr
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_column")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Month View Title & Fast Add Trigger Row
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayedMonthYearName = remember(viewModel.selectedDateInMillis) {
                    SimpleDateFormat("'Tháng' MM, yyyy", Locale("vi", "VN")).format(Date(viewModel.selectedDateInMillis))
                }
                Column {
                    Text(
                        text = displayedMonthYearName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Lịch Trình Cá Nhân Cozy",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Smart Add AI Float Button
                    FilledIconButton(
                        onClick = { viewModel.showQuickAddDialog = true },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("smart_add_calendar_icon"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFFB3A0))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Smart AI",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Regular Manual Add Form toggler
                    FilledIconButton(
                        onClick = { showManualAddForm = !showManualAddForm },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("manual_add_calendar_icon"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFCFEADF))
                    ) {
                        Icon(
                            imageVector = if (showManualAddForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Manual Add",
                            tint = TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 1.2 Month & Year Quick Customizer Navigation Panel
        item {
            val calTmp = Calendar.getInstance()
            calTmp.timeInMillis = viewModel.selectedDateInMillis
            val currentYear = calTmp.get(Calendar.YEAR)
            val currentMonth = calTmp.get(Calendar.MONTH) // 0-indexed
            
            // State for custom picker dialog to jump to any year/month directly
            var showPickerByClick by remember { mutableStateOf(false) }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CreamSurface)
                    .border(BorderStroke(1.dp, Color(0xFFE5D9C4)), RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigate Month Back
                    FilledIconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = viewModel.selectedDateInMillis
                            cal.add(Calendar.MONTH, -1)
                            viewModel.selectedDateInMillis = cal.timeInMillis
                        },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFFFFF2EE),
                            contentColor = Color(0xFFFF8A75)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tháng trước",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Jump Month & Year Custom Selector Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showPickerByClick = true }
                            .padding(horizontal = 8.dp)
                    ) {
                        val sdfHeader = remember(viewModel.selectedDateInMillis) {
                            SimpleDateFormat("MMMM - yyyy", Locale("vi", "VN"))
                        }
                        val formattedHeader = sdfHeader.format(Date(viewModel.selectedDateInMillis))
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("vi", "VN")) else it.toString() }
                        Text(
                            text = formattedHeader,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Chọn",
                                tint = Color(0xFFFF8A75),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nhấp để đổi nhanh 📅",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }

                    // Navigate Month Forward
                    FilledIconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = viewModel.selectedDateInMillis
                            cal.add(Calendar.MONTH, 1)
                            viewModel.selectedDateInMillis = cal.timeInMillis
                        },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFFFFF2EE),
                            contentColor = Color(0xFFFF8A75)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Tháng sau",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color(0xFFF2EAD8),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigate Year Back
                    TextButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = viewModel.selectedDateInMillis
                            cal.add(Calendar.YEAR, -1)
                            viewModel.selectedDateInMillis = cal.timeInMillis
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF8A75))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Năm cũ",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Năm Trước", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(Color(0xFFE5D9C4))
                    )

                    // Navigate Year Forward
                    TextButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = viewModel.selectedDateInMillis
                            cal.add(Calendar.YEAR, 1)
                            viewModel.selectedDateInMillis = cal.timeInMillis
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF8A75))
                    ) {
                        Text("Năm Sau", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Năm mới",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Elegant Picker Dialog for Quick Selecting Month and Year customisations
            if (showPickerByClick) {
                var tempSelectedMonth by remember { mutableStateOf(currentMonth) }
                var tempSelectedYear by remember { mutableStateOf(currentYear) }
                
                AlertDialog(
                    onDismissRequest = { showPickerByClick = false },
                    title = {
                        Text(
                            text = "Tùy Chỉnh Thời Gian Lịch 📅",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Year selection segment
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Chọn Năm:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { tempSelectedYear-- },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Trở lại")
                                    }
                                    
                                    Text(
                                        text = "$tempSelectedYear",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF8A75)
                                    )
                                    
                                    IconButton(
                                        onClick = { tempSelectedYear++ },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Tiếp theo")
                                    }
                                }
                            }

                            // Month selection grid
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Chọn Tháng:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                val monthLabels = listOf(
                                    "Thg 1", "Thg 2", "Thg 3", "Thg 4",
                                    "Thg 5", "Thg 6", "Thg 7", "Thg 8",
                                    "Thg 9", "Thg 10", "Thg 11", "Thg 12"
                                )
                                val chunks = monthLabels.chunked(4)
                                chunks.forEachIndexed { rowIndex, rowList ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowList.forEachIndexed { colIndex, formattedMonthName ->
                                            val mIndex = rowIndex * 4 + colIndex
                                            val isSelected = tempSelectedMonth == mIndex
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) Color(0xFFFFB3A0) else Color(0xFFFFF9F6))
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) Color(0xFFFFB3A0) else Color(0xFFFFECE6),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { tempSelectedMonth = mIndex },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = formattedMonthName,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else TextDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = viewModel.selectedDateInMillis
                                cal.set(Calendar.YEAR, tempSelectedYear)
                                cal.set(Calendar.MONTH, tempSelectedMonth)
                                viewModel.selectedDateInMillis = cal.timeInMillis
                                showPickerByClick = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                        ) {
                            Text("Áp Dụng", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPickerByClick = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
                        ) {
                            Text("Hủy")
                        }
                    },
                    containerColor = CreamBackground,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }

        // 1.5. Calendar Options segment chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "day" to "🗓️ Theo Ngày",
                    "week" to "📅 Xem Tuần",
                    "month" to "📅 Lịch Tháng",
                    "agenda" to "📋 Toàn Bộ Lịch"
                ).forEach { (viewId, label) ->
                    val isSelected = currentCalendarView == viewId
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFFFB3A0) else CreamSurface)
                            .border(1.dp, if (isSelected) Color(0xFFFFB3A0) else Color(0xFFE5D9C4), RoundedCornerShape(12.dp))
                            .clickable { currentCalendarView = viewId }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextDark
                        )
                    }
                }
            }
        }

        // 2. Weekly Calendar Strips (Only for "day" view)
        if (currentCalendarView == "day") {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(weekDays) { date ->
                        val sdfDay = SimpleDateFormat("dd", Locale.getDefault())
                        val sdfDayName = SimpleDateFormat("E", Locale("vi", "VN"))
                        val isSelected = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) == 
                                         SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(viewModel.selectedDateInMillis))

                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFFFFB3A0) else CreamSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFFFB3A0) else Color(0xFFE5D9C4),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectedDateInMillis = date.time }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sdfDayName.format(date).uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                                Text(
                                    text = sdfDay.format(date),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // Expanded Month Grid Calendar (Only for "month" view)
        if (currentCalendarView == "month") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
                    border = BorderStroke(1.dp, Color(0xFFE5D9C4))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val currentMonthName = SimpleDateFormat("MMMM yyyy", Locale("vi", "VN"))
                            .format(Date(viewModel.selectedDateInMillis))
                        Text(
                            text = currentMonthName.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A75)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val headers = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
                            headers.forEach { h ->
                                Text(
                                    text = h,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        val sdfDayCompare = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val selectedDayStr = sdfDayCompare.format(Date(viewModel.selectedDateInMillis))
                        
                        val chunks = monthCalendarDays.chunked(7)
                        chunks.forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                repeat(7) { colIndex ->
                                    val date = week.getOrNull(colIndex)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (date != null) {
                                            val dayStr = SimpleDateFormat("d", Locale.getDefault()).format(date)
                                            val isSelected = sdfDayCompare.format(date) == selectedDayStr
                                            
                                            val dayEventsCount = events.count {
                                                sdfDayCompare.format(Date(it.startTime)) == sdfDayCompare.format(date)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) Color(0xFFFFB3A0) else Color.Transparent)
                                                    .clickable { viewModel.selectedDateInMillis = date.time }
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                    Text(
                                                        text = dayStr,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else TextDark
                                                    )
                                                    if (dayEventsCount > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else Color(0xFFFF8A75))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Optional Manual Add Expansion Form
        item {
            AnimatedVisibility(
                visible = showManualAddForm,
                label = "ManualForm"
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_event_form"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Lên Lịch Thủ Công ✏️",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = manualTitle,
                            onValueChange = { manualTitle = it },
                            label = { Text("Tên sự kiện / Việc cần làm") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_title_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = manualDesc,
                            onValueChange = { manualDesc = it },
                            label = { Text("Ghi chú bổ sung (tùy chọn)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Category choices
                        Text(
                            text = "Danh Mục Gợi Ý:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categories = listOf("study" to "📚 Học", "self-care" to "🧘 Thư giãn", "work" to "💼 Làm việc", "routine" to "🌱 Thói quen")
                            categories.forEach { (catId, label) ->
                                val catSelected = manualCategory == catId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (catSelected) Color(0xFFFFB3A0) else CreamBackground)
                                        .clickable { manualCategory = catId }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (catSelected) Color.White else TextDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Set times (simple sliders or text fields)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = manualStartHour.toString(),
                                onValueChange = { manualStartHour = it.toIntOrNull() ?: 9 },
                                label = { Text("Giờ (0-23)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = manualDuration.toString(),
                                onValueChange = { manualDuration = it.toIntOrNull() ?: 60 },
                                label = { Text("Số Phút") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Choose day offset
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = manualOffsetDays == 1,
                                onCheckedChange = { manualOffsetDays = if (it) 1 else 0 }
                            )
                            Text(
                                text = "Lên lịch vào Ngày Mai (Thay vì Hôm Nay)",
                                fontSize = 12.sp,
                                color = TextDark
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (manualTitle.trim().isNotEmpty()) {
                                    viewModel.addDirectEvent(
                                        title = manualTitle,
                                        category = manualCategory,
                                        startHour = manualStartHour,
                                        startMin = manualStartMin,
                                        duration = manualDuration,
                                        desc = manualDesc,
                                        offsetDays = manualOffsetDays
                                    )
                                    // Reset fields
                                    manualTitle = ""
                                    manualDesc = ""
                                    showManualAddForm = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("form_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                        ) {
                            Text("Tạo Lịch Cozy", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Agenda list of selected day
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentCalendarView == "agenda") "Toàn Bộ Lịch Trình" else "Lịch Trình Chi Tiết",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (currentCalendarView != "agenda") {
                    Text(
                        text = selectedFormatted,
                        fontSize = 11.sp,
                        color = Color(0xFFFF8A75),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (currentCalendarView == "week") {
            items(weekDays) { date ->
                val sdfDayCompare = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayStr = SimpleDateFormat("EEEE, dd/MM", Locale("vi", "VN")).format(date)
                
                val dayEvents = events.filter {
                    sdfDayCompare.format(Date(it.startTime)) == sdfDayCompare.format(date)
                }.sortedBy { it.startTime }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface),
                    border = BorderStroke(1.dp, Color(0xFFE5D9C4))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = dayStr,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A75)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (dayEvents.isEmpty()) {
                            Text(
                                text = "Không có sự kiện nào ✨",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            dayEvents.forEach { event ->
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val sFormatted = timeFormat.format(Date(event.startTime))
                                val eFormatted = timeFormat.format(Date(event.endTime))
                                val eventColor = safeParseColor(event.colorHex)
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(eventColor)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = event.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark,
                                                textDecoration = if (event.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                            )
                                            Text(
                                                text = "Từ $sFormatted đến $eFormatted",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.completeEvent(event) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (event.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = "Complete",
                                                tint = if (event.isCompleted) Color(0xFF388E3C) else Color(0xFFFFB3A0),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteEvent(event) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFFF8A75),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val displayEventsList = if (currentCalendarView == "agenda") {
                events.sortedBy { it.startTime }
            } else {
                filteredEvents
            }

            if (displayEventsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CreamSurface)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Empty",
                                tint = TextMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (currentCalendarView == "agenda") "Chưa có sự kiện nào trong lịch ~" else "Trống lịch cho ngày này rồi ~",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                            Text(
                                text = "Bấm nút \"Smart Add\" để AI lên lịch thật dễ thương nha!",
                                fontSize = 11.sp,
                                color = TextMuted.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(displayEventsList) { event ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val sFormatted = timeFormat.format(Date(event.startTime))
                val eFormatted = timeFormat.format(Date(event.endTime))
                val dateFormatted = sdfDate.format(Date(event.startTime))
                val eventColor = safeParseColor(event.colorHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, eventColor, RoundedCornerShape(20.dp))
                        .testTag("calendar_event_item_${event.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(eventColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = event.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    textDecoration = if (event.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = if (currentCalendarView == "agenda") {
                                        "$dateFormatted | Từ $sFormatted đến $eFormatted (${event.category.uppercase()})"
                                    } else {
                                        "Từ $sFormatted đến $eFormatted (${event.category.uppercase()})"
                                    },
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                if (event.description.isNotEmpty()) {
                                    Text(
                                        text = event.description,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { viewModel.completeEvent(event) }) {
                                Icon(
                                    imageVector = if (event.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Complete",
                                    tint = if (event.isCompleted) Color(0xFF388E3C) else Color(0xFFFFB3A0)
                                )
                            }
                            IconButton(onClick = { viewModel.deleteEvent(event) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF8A75)
                                )
                            }
                        }
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
