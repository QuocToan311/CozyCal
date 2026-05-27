package com.example.ui.screens

import kotlinx.coroutines.launch
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PetRoomCanvas
import com.example.ui.components.GoogleOAuthWebViewDialog
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) } // 0: Live Mascot & Auth panel, 1: Select Comp, 2: Name Comp, 3: Goal & Calendar, 4: Notification approval

    // User Data capture
    var userName by remember { mutableStateOf("Quốc Toàn") }
    var petType by remember { mutableStateOf("Cat") }
    var petPersonality by remember { mutableStateOf("Thư thái") }
    var petName by remember { mutableStateOf("Mochi") }
    var productivityGoal by remember { mutableStateOf("Tập trung sâu 🧘") }
    var isCalendarConnected by remember { mutableStateOf(false) }

    // Auth flows
    var isRegisterMode by remember { mutableStateOf(false) }
    var forgotPasswordRequested by remember { mutableStateOf(false) }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }

    // Google Sign-In customization flows
    val coroutineScope = rememberCoroutineScope()
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showWebViewLogin by remember { mutableStateOf(false) }
    val googleClientId by viewModel.googleClientId.collectAsState()
    var customGoogleEmail by remember { mutableStateOf("") }
    var customGoogleName by remember { mutableStateOf("") }
    var isSigningInGoogle by remember { mutableStateOf(false) }
    var showCustomEmailField by remember { mutableStateOf(false) }

    val petStateFromDb by viewModel.petState.collectAsState()

    LaunchedEffect(petStateFromDb) {
        petStateFromDb?.let { pet ->
            userName = pet.userName
            petType = pet.petType
            petPersonality = pet.petPersonality
            petName = pet.name
            productivityGoal = pet.productivityGoal
            isCalendarConnected = pet.isCalendarConnected
            
            // Auto advance to pet selection if user is logged in but has not finished onboarding
            if (pet.isLoggedIn && !pet.hasCompletedOnboarding && step == 0) {
                step = 1
            }
        }
    }

    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFECE6), // Light Peach
            Color(0xFFE5E3F7), // Soft Lavender
            Color(0xFFFFFDF9)  // Cozy Cream
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP PROGRESS DOTS (top)
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { d ->
                    Box(
                        modifier = Modifier
                            .size(if (step == d) 16.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (step == d) Color(0xFFFFB3A0) else Color(0xFFD7CCC8))
                    )
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "OnboardingSteps"
            ) { currentStep ->
                when (currentStep) {
                    0 -> {
                        // --- 1. INTRO / WELCOME & COZY AUTHENTICATION ---
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "CozyCal",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.testTag("onboarding_app_title")
                            )
                            Text(
                                text = "Nơi cuộc sống bận rộn gặp sự bình yên an tĩnh. Cùng quản lý thời gian & tự chăm sóc bản thân với thú cưng ảo của bạn. ✨🌱",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = TextMuted,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Glassmorphic interactive pet animation preview
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = CreamSurface.copy(alpha = 0.8f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    PetRoomCanvas(
                                        isSleeping = false,
                                        activeAccessories = setOf("pillow"),
                                        petType = "Cat",
                                        onPetTap = {}
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFB3A0))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Mochi mỉm cười 🌸", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Interactive input form fields
                            if (forgotPasswordRequested) {
                                // Forgot Pass Flow
                                Text(
                                    text = "Quên Mật Khẩu?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                OutlinedTextField(
                                    value = authEmail,
                                    onValueChange = { authEmail = it },
                                    label = { Text("Email liên kết") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = {
                                        forgotPasswordRequested = false
                                        viewModel.dismissSparkle()
                                        viewModel.completeOnboarding(userName, petName, petType, petPersonality, productivityGoal, isCalendarConnected)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                                ) {
                                    Text("Gửi Email Khôi Phục")
                                }
                                TextButton(onClick = { forgotPasswordRequested = false }) {
                                    Text("Quay lại đăng nhập", color = TextMuted)
                                }
                            } else {
                                // Unified Login / Register Form
                                Text(
                                    text = if (isRegisterMode) "Đăng Ký Tài Khoản Mới" else "Chào mừng trở lại!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                if (isRegisterMode) {
                                    OutlinedTextField(
                                        value = userName,
                                        onValueChange = { userName = it },
                                        label = { Text("Tên của bạn") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                OutlinedTextField(
                                    value = authEmail,
                                    onValueChange = { authEmail = it },
                                    label = { Text("Địa chỉ Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = authPassword,
                                    onValueChange = { authPassword = it },
                                    label = { Text("Mật khẩu") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                                        Text(
                                            text = if (isRegisterMode) "Đã có tài khoản? Đăng nhập" else "Tạo tài khoản mới 🌱",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFF8A75)
                                        )
                                    }
                                    TextButton(onClick = { forgotPasswordRequested = true }) {
                                        Text("Quên mật khẩu?", fontSize = 11.sp, color = TextMuted)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick CTA buttons
                                Button(
                                    onClick = {
                                        if (isRegisterMode) {
                                            viewModel.registerNewUser(name = userName, email = authEmail, passwordEntered = authPassword) {
                                                step = 1
                                            }
                                        } else {
                                            viewModel.loginUser(email = authEmail, passwordEntered = authPassword)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("onboarding_continue_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                                ) {
                                    Text(if (isRegisterMode) "Đăng Ký & Chọn Đồng Hành 🌱" else "Đăng Nhập 🔑", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            userName = "Guest Scholar"
                                            viewModel.currentUserEmail.value = "guest"
                                            step = 1
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECEFF1)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Chế độ Khách", color = TextDark, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { showGoogleDialog = true },
                                        modifier = Modifier.weight(1.2f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFECE5D9))
                                    ) {
                                        Icon(imageVector = Icons.Default.Email, contentDescription = "", tint = TextDark, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Google / Apple", color = TextDark, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- 2. SELECT虚拟 PET PARTNER COMPANION ---
                        val petOptions = listOf(
                            PetInfo("Cat", "Mochi 🐱", "Ngọt ngào, hay làm nũng, yêu thích ngủ nướng và cuộn tròn ngắm mưa.", Color(0xFFFFE0B2)),
                            PetInfo("Bunny", "Luna 🐰", "Nhẹ nhàng, tinh nghịch, thích tiếng cỏ bay và rải hạt hoa dẻ.", Color(0xFFFFCDD2)),
                            PetInfo("Fox", "Milo 🦊", "Thông minh, mê sách, có chiếc đuôi to ấm sực giúp giữ ấm ngày gió.", Color(0xFFFFCCBC)),
                            PetInfo("Bear", "Pipo 🐻", "Chubby, an tâm, thích bánh mật ong ấm áp xào xạc cạnh bếp sưởi.", Color(0xFFD7CCC8)),
                            PetInfo("Penguin", "Pingu 🐧", "Tò mò, vui vẻ, khoái dạo chơi và đập cánh bành bạch khi bạn tập trung.", Color(0xFFCFD8DC)),
                            PetInfo("Hamster", "Chippi 🐹", "Chubby, chạy liên tục thói quen và thích má bánh ú ngậm dẻ.", Color(0xFFFFE082))
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Chọn Bạn Đồng Hành 🐾",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Chọn loài thú cưng ảo sẽ cùng sống chung, nhắc nhở bạn hít thở dịu mát và rèn luyện thói quen mỗi ngày.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )

                            // Drawn pet preview live inside Onboarding Screen
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = CreamSurface)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    PetRoomCanvas(
                                        isSleeping = false,
                                        activeAccessories = emptySet(),
                                        petType = petType,
                                        onPetTap = {}
                                    )
                                }
                            }

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(petOptions) { option ->
                                    val isSelected = petType == option.id
                                    Box(
                                        modifier = Modifier
                                            .width(95.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) Color(0xFFFFB3A0) else Color.White)
                                            .clickable {
                                                petType = option.id
                                                petName = option.name.split(" ")[0]
                                            }
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = option.name.split(" ")[1], fontSize = 22.sp)
                                            Text(
                                                text = option.name.split(" ")[0],
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else TextDark
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic personality detail description
                            val currentPetDetail = petOptions.find { it.id == petType }
                            currentPetDetail?.let {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CreamSurface.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "Tính Cách: ${it.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                        Text(text = it.desc, fontSize = 11.sp, color = TextMuted, lineHeight = 14.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { step = 0 },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7CCC8))
                                ) {
                                    Text("Quay Lại")
                                }
                                Button(
                                    onClick = { step = 2 },
                                    modifier = Modifier.weight(1.5f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                                ) {
                                    Text("Tiếp Tục ❤️")
                                }
                            }
                        }
                    }

                    2 -> {
                        // --- 3. NAME THE virtual Companion & typing preview ---
                        val nameSuggestions = when (petType) {
                            "Cat" -> listOf("Mochi", "Nori", "Bông", "Meo", "Kem")
                            "Bunny" -> listOf("Luna", "Mây", "Thỏ", "Bơ", "Rốt")
                            "Fox" -> listOf("Milo", "Cáo", "Cam", "Sunny", "Lá")
                            "Bear" -> listOf("Pipo", "Gấu", "Nâu", "Béo", "Mật")
                            "Hamster" -> listOf("Chippi", "Chuột", "Ú", "Hạt", "Xu")
                            else -> listOf("Mochi", "Luna", "Milo", "Pipo", "Chippi")
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Đặt Tên Người Bạn Nhỏ 🏷️",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Mochi thích cái tên nào ngọt ngào một chút nhé! Tên của người bạn sẽ xuất hiện xuyên suốt hành trình tự chăm sóc bản thân.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )

                            // Animated typing preview text
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(CreamSurface)
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🐾 $petName 🐾",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8A75)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "“Chào mẹ $userName! Từ giờ hãy gọi con là $petName nhé! Chụt chụt!” ✨😽",
                                    fontSize = 12.sp,
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            OutlinedTextField(
                                value = petName,
                                onValueChange = { petName = it },
                                label = { Text("Tên thú cưng") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = "") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Gợi ý:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                                nameSuggestions.forEach { nameVal ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFECE6))
                                            .border(1.dp, Color(0xFFFFB3A0).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .clickable { petName = nameVal }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = nameVal, fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { step = 1 },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7CCC8))
                                ) {
                                    Text("Quay Lại")
                                }
                                Button(
                                    onClick = { step = 3 },
                                    modifier = Modifier.weight(1.5f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                                ) {
                                    Text("Tiếp Tục ❤️")
                                }
                            }
                        }
                    }

                    3 -> {
                        // --- 4. CHOOSE PROD GOALS & INTEGRATE CALENDAR ---
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Mục Tiêu & Kết Nối Lịch Lập Trình 🗓️",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Đồng hành rèn luyện nếp sống bình yên tự nhiên và đồng bộ hóa lịch hai chiều Google Calendar mượt mà.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val goals = listOf(
                                    GoalItem("study", "Tập trung sâu 🧘", "Đọc sách thong thả, làm việc kĩ thuật sâu và tập trung 25 phút."),
                                    GoalItem("self-care", "Thói quen lành mạnh 🏋️", "Dành thời gian thở sâu, ăn ngon, uống ấm mật ong sương sương."),
                                    GoalItem("routine", "Sắp xếp lịch trình an nhiên 🗓️", "Nắm rõ các mốc thời gian, an vui tận hưởng ngày nghỉ.")
                                )

                                goals.forEach { item ->
                                    val isSelected = productivityGoal.contains(item.title)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) Color(0xFFFFB3A0) else Color.White)
                                            .clickable { productivityGoal = item.title }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = if (item.id == "study") "🧘" else if (item.id == "self-care") "🏋️" else "🗓️", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else TextDark)
                                            Text(text = item.desc, fontSize = 10.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted, lineHeight = 12.sp)
                                        }
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "",
                                            tint = if (isSelected) Color.White else Color(0xFFFFB3A0)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Google calendar linking simulation panel
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, if (isCalendarConnected) Color(0xFFCFEADF) else Color(0xFFFFECE6)),
                                colors = CardDefaults.cardColors(containerColor = if (isCalendarConnected) Color(0xFFE8F5E9) else Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(imageVector = Icons.Default.Schedule, contentDescription = "", tint = Color(0xFFFFB3A0))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Đồng bộ Google Calendar 🔄", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                            Text(
                                                text = if (isCalendarConnected) "Đã tạo liên kết tự động hai chiều!" else "Tự động gửi/nhận sự kiện sắp tới cực an lành.",
                                                fontSize = 9.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isCalendarConnected,
                                        onCheckedChange = { isCalendarConnected = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF388E3C), checkedTrackColor = Color(0xFFC8E6C9))
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { step = 2 },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7CCC8))
                                ) {
                                    Text("Quay Lại")
                                }
                                Button(
                                    onClick = { step = 4 },
                                    modifier = Modifier.weight(1.5f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                                ) {
                                    Text("Tiếp Tục 🗓️")
                                }
                            }
                        }
                    }

                    4 -> {
                        // --- 5. COMPANION PERMISSIONS & CELEBRATION DISMISSAL ---
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Beautiful celebration virtual card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = CreamSurface)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    PetRoomCanvas(
                                        isSleeping = false,
                                        activeAccessories = setOf("lamp", "rug", "plant"),
                                        petType = petType,
                                        onPetTap = {}
                                    )
                                }
                            }

                            Text(
                                text = "Bật Thông Báo Nhắc Nhở Nhẹ Nhàng 🔔",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Cho phép $petName thỉnh thoảng khều tay nhắc bạn nghỉ ngơi làm cốc trà âm ấm, dãn gân cốt hay báo một cuộc họp sắp diễn ra an bình.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            // Nice approval permission mockup check row
                            var pushApproved by remember { mutableStateOf(true) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .clickable { pushApproved = !pushApproved }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CircleNotifications, contentDescription = "", tint = Color(0xFFFFB3A0))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gửi thông báo dịu mát hàng ngày", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                                Switch(
                                    checked = pushApproved,
                                    onCheckedChange = { pushApproved = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFB3A0))
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.completeOnboarding(
                                        userName = userName,
                                        petName = petName,
                                        petType = petType,
                                        petPersonality = petPersonality,
                                        productivityGoal = productivityGoal,
                                        isCalendarConnected = isCalendarConnected
                                    )
                                    if (isCalendarConnected) {
                                        viewModel.performCalendarSyncSimulation()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_complete_finish"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0))
                            ) {
                                Text("Vào Ngôi Nhà CozyCal Thôi! ✨🏡", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGoogleDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { if (!isSigningInGoogle) showGoogleDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CreamSurface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        if (isSigningInGoogle) {
                            Spacer(modifier = Modifier.height(12.dp))
                            CircularProgressIndicator(
                                color = Color(0xFFFFB3A0),
                                strokeWidth = 3.5.dp,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Đang kết nối an toàn với Google...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Thiết lập quyền truy cập bảo mật và đồng bộ lịch mượt mà.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        } else {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Draw a miniature G
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF4285F4)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Đăng nhập bằng Google",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "đăng nhập bảo mật một chạm",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFECE5D9), thickness = 0.8.dp)

                            Button(
                                onClick = { showWebViewLogin = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🌐 ", fontSize = 16.sp, color = Color.White)
                                    Text("Đăng nhập bằng Google Thật", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }

                            Text(
                                text = "Hoặc chọn tài khoản thử nghiệm nhanh:",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )

                            // List of Accounts
                            val accounts = listOf(
                                Pair("duongquoctoan3101@gmail.com", "Quốc Toàn"),
                                Pair("cozycal.user@gmail.com", "Cozy Buddy")
                            )

                            accounts.forEach { (email, name) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            isSigningInGoogle = true
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(1200)
                                                isSigningInGoogle = false
                                                showGoogleDialog = false
                                                viewModel.loginWithGoogle(email, name) {
                                                    step = 1
                                                }
                                            }
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar representation
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFB3A0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = email,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            // Custom Input option
                            if (!showCustomEmailField) {
                                TextButton(
                                    onClick = { showCustomEmailField = true },
                                    modifier = Modifier.align(Alignment.Start)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFFF8A75)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sử dụng tài khoản khác...", fontSize = 12.sp, color = Color(0xFFFF8A75))
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customGoogleEmail,
                                        onValueChange = { customGoogleEmail = it },
                                        label = { Text("Địa chỉ Gmail mới", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                    )
                                    OutlinedTextField(
                                        value = customGoogleName,
                                        onValueChange = { customGoogleName = it },
                                        label = { Text("Tên tài khoản mới", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { showCustomEmailField = false }) {
                                            Text("Hủy", fontSize = 12.sp, color = TextMuted)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (customGoogleEmail.isNotEmpty()) {
                                                    isSigningInGoogle = true
                                                    val finalName = if (customGoogleName.isEmpty()) "G-User" else customGoogleName
                                                    coroutineScope.launch {
                                                        kotlinx.coroutines.delay(1200)
                                                        isSigningInGoogle = false
                                                        showGoogleDialog = false
                                                        viewModel.loginWithGoogle(customGoogleEmail, finalName) {
                                                            step = 1
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB3A0)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Liên kết", fontSize = 12.sp, color = Color.White)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFECE5D9), thickness = 0.8.dp)

                            TextButton(
                                onClick = { showGoogleDialog = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Đóng", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
    }
}

data class PetInfo(
    val id: String,
    val name: String,
    val desc: String,
    val color: Color
)

data class GoalItem(
    val id: String,
    val title: String,
    val desc: String
)
