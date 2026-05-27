package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PetRoomCanvas
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FocusScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Dynamic background color shifting based on active scene!
    val focusBgColor by animateColorAsState(
        targetValue = when (viewModel.currentFocusScene) {
            "rain" -> Color(0xFFD6EDF8) // Muted summer rain blue
            "forest" -> Color(0xFFCFEADF) // Minty forest green
            "fireplace" -> Color(0xFFFFECE6) // Cozy warm orange-peach
            else -> CreamBackground
        },
        animationSpec = tween(1000),
        label = "FocusColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_screen_column")
            .background(focusBgColor)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Scene chooser top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CreamSurface.copy(alpha = 0.8f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FocusSceneButton(
                icon = "🌦️",
                label = "Mưa",
                isSelected = viewModel.currentFocusScene == "rain",
                onClick = { viewModel.currentFocusScene = "rain" }
            )
            FocusSceneButton(
                icon = "🌲",
                label = "Rừng",
                isSelected = viewModel.currentFocusScene == "forest",
                onClick = { viewModel.currentFocusScene = "forest" }
            )
            FocusSceneButton(
                icon = "🔥",
                label = "Sưởi",
                isSelected = viewModel.currentFocusScene == "fireplace",
                onClick = { viewModel.currentFocusScene = "fireplace" }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Emotional Pet Companion sleeping card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .testTag("focus_pet_dorm"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val petState by viewModel.petState.collectAsState()
                val accessoriesSet = remember(petState) {
                    petState?.activeAccessories?.split(",")?.toSet() ?: emptySet()
                }

                PetRoomCanvas(
                    isSleeping = viewModel.isFocusTimerRunning,
                    activeAccessories = accessoriesSet,
                    petType = petState?.petType ?: "Cat"
                )

                // Sleep speech bubble overlay inside the focus card
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (viewModel.isFocusTimerRunning) {
                                "Khò khò... Mochi đang ngủ ngoan cạnh bạn đó... 😴"
                            } else {
                                "Chọn nhạc rồi bắt đầu tập trung thôi nào! 🎵"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 3. Countdown timer dial
        val minutes = viewModel.focusTimeLeftSeconds / 60
        val seconds = viewModel.focusTimeLeftSeconds % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.testTag("focus_countdown_text")
            )
            Text(
                text = if (viewModel.isFocusTimerRunning) "HÃY TẬP TRUNG NHA" else "SẴN SÀNG FOCUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 4. Fast intervals selection row (only shown if timer is not running)
        AnimatedVisibility(
            visible = !viewModel.isFocusTimerRunning,
            label = "PresetToggles"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    val isSelected = viewModel.focusSelectedMinutes == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFFFFB3A0) else CreamSurface)
                            .clickable { viewModel.setFocusDuration(mins) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mins}p",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom focus duration slider (tactile & cozy)
        AnimatedVisibility(
            visible = !viewModel.isFocusTimerRunning,
            label = "CustomSlider"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CreamSurface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tự chỉnh thời gian ⏱️",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilledIconButton(
                            onClick = { viewModel.setFocusDuration((viewModel.focusSelectedMinutes - 5).coerceAtLeast(1)) },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFFE5E0))
                        ) {
                            Text("-5", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8A75))
                        }
                        FilledIconButton(
                            onClick = { viewModel.setFocusDuration((viewModel.focusSelectedMinutes - 1).coerceAtLeast(1)) },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFFE5E0))
                        ) {
                            Text("-1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8A75))
                        }
                        
                        Text(
                            text = "${viewModel.focusSelectedMinutes} phút",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A75),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        
                        FilledIconButton(
                            onClick = { viewModel.setFocusDuration((viewModel.focusSelectedMinutes + 1).coerceAtMost(180)) },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFFE5E0))
                        ) {
                            Text("+1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8A75))
                        }
                        FilledIconButton(
                            onClick = { viewModel.setFocusDuration((viewModel.focusSelectedMinutes + 5).coerceAtMost(180)) },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFFE5E0))
                        ) {
                            Text("+5", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8A75))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = viewModel.focusSelectedMinutes.toFloat(),
                    onValueChange = { mins ->
                        viewModel.setFocusDuration(mins.toInt())
                    },
                    valueRange = 1f..180f,
                    steps = 178,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFB3A0),
                        activeTrackColor = Color(0xFFFFB3A0),
                        inactiveTrackColor = Color(0xFFE5D9C4)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("custom_focus_duration_slider")
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. Timer toggle button (Play/Pause)
        FilledIconButton(
            onClick = { viewModel.toggleFocusTimer() },
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .testTag("focus_timer_toggle_btn"),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (viewModel.isFocusTimerRunning) Color(0xFFFF8A75) else Color(0xFFFFB3A0)
            )
        ) {
            Icon(
                imageVector = if (viewModel.isFocusTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Trigger Timer",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun RowScope.FocusSceneButton(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFFFFB3A0) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextDark
            )
        }
    }
}
