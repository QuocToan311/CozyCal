package com.example

import android.content.Context
import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiClient
import com.example.ui.components.ScannedPreviewDialog
import com.example.ui.components.SmartAddDialog
import com.example.ui.components.SparkleNotification
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }
    
    setContent {
      MyApplicationTheme {
        val petState by viewModel.petState.collectAsState()

        if (petState == null) {
          // Loading app state to prevent flicker and secure routing
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = Color(0xFFFFB3A0))
          }
        } else if (!petState!!.hasCompletedOnboarding || !petState!!.isLoggedIn) {
          // Fullscreen Onboarding & Authorization Experience
          Box(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen(viewModel = viewModel)

            // Dynamic float notification over onboarding
            SparkleNotification(
              message = viewModel.sparkleMessage,
              onDismiss = { viewModel.dismissSparkle() }
            )
          }
        } else {
          Scaffold(
            modifier = Modifier
              .fillMaxSize()
              .testTag("app_scaffold"),
            bottomBar = {
              CozyBottomNavigationBar(
                currentTab = viewModel.currentTab,
                onTabSelected = { viewModel.currentTab = it }
              )
            },
            contentWindowInsets = WindowInsets.safeDrawing
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
            ) {
              
              // --- Core Tab Switching Screens ---
              Crossfade(
                targetState = viewModel.currentTab,
                animationSpec = tween(400),
                label = "TabTransition"
              ) { tab ->
                when (tab) {
                  "home" -> HomeScreen(viewModel = viewModel)
                  "calendar" -> CalendarScreen(viewModel = viewModel)
                  "focus" -> FocusScreen(viewModel = viewModel)
                  "pet" -> PetScreen(viewModel = viewModel)
                  "profile" -> ProfileScreen(viewModel = viewModel)
                }
              }

              // --- Fullscreen Floating Overlay Loader (Magic AI is working) ---
              if (viewModel.isAIParsing) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .testTag("ai_loading_overlay"),
                  contentAlignment = Alignment.Center
                ) {
                  Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier.padding(32.dp)
                  ) {
                    Column(
                      modifier = Modifier.padding(24.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      CircularProgressIndicator(
                        color = Color(0xFFFFB3A0),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                      )
                      Spacer(modifier = Modifier.height(16.dp))
                      Text(
                        text = "Đang dọn phòng & dùng phép thuật AI phân tích...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("ai_loading_text")
                      )
                      Text(
                        text = "Mochi đang chăm chú nhìn chiếc thìa ma thuật ✨",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                      )
                    }
                  }
                }
              }

              // --- Floating Interactive Sparkle Notifications toast ---
              Box(
                modifier = Modifier
                  .align(Alignment.TopCenter)
                  .fillMaxWidth()
              ) {
                SparkleNotification(
                  message = viewModel.sparkleMessage,
                  onDismiss = { viewModel.dismissSparkle() }
                )
              }

              // --- Floating Smart AI NLP/OCR Add Input Dialog ---
              SmartAddDialog(
                isOpen = viewModel.showQuickAddDialog,
                onDismiss = { viewModel.showQuickAddDialog = false },
                inputText = viewModel.quickAddInputText,
                onInputTextChange = { viewModel.quickAddInputText = it },
                onSubmitPrompt = { viewModel.executeNaturalLanguageSmartAdd() },
                onScanSampleImage = { bitmap, hint -> viewModel.scanImageScheduleWithAI(bitmap, hint) },
                onScanTextFile = { text -> viewModel.scanTextFileWithAI(text) },
                isApiKeyConfigured = GeminiClient.isApiKeyAvailable()
              )

              // --- Floating AI image scanned preview list table confirmation dialog ---
              ScannedPreviewDialog(
                isOpen = viewModel.showPreviewEventsDialog,
                events = viewModel.parsedPreviewEvents,
                onDismiss = {
                  viewModel.parsedPreviewEvents = emptyList()
                  viewModel.showPreviewEventsDialog = false
                },
                onConfirm = { editedEvents -> viewModel.confirmImportScannedEvents(editedEvents) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CozyBottomNavigationBar(
  currentTab: String,
  onTabSelected: (String) -> Unit
) {
  // Navigation pill indicators respect notch safety padding bounds automatically
  NavigationBar(
    containerColor = Color(0xFFFFFDF9), // Cream surface color matches layout style
    contentColor = Color(0xFF3D3A36),
    tonalElevation = 8.dp,
    modifier = Modifier
      .navigationBarsPadding() // Notch & gesture safety
      .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
      .testTag("cozy_bottom_nav_bar")
  ) {
    NavigationBarItem(
      selected = currentTab == "home",
      onClick = { onTabSelected("home") },
      icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
      label = { Text("Trang chủ", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color(0xFF5D4037),
        indicatorColor = Color(0xFFFFB3A0) // Peach
      ),
      modifier = Modifier.testTag("nav_btn_home")
    )
    NavigationBarItem(
      selected = currentTab == "calendar",
      onClick = { onTabSelected("calendar") },
      icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Schedule") },
      label = { Text("Lịch trình", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color(0xFF5D4037),
        indicatorColor = Color(0xFFFFB3A0) // Peach
      ),
      modifier = Modifier.testTag("nav_btn_calendar")
    )
    NavigationBarItem(
      selected = currentTab == "focus",
      onClick = { onTabSelected("focus") },
      icon = { Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = "Focus") },
      label = { Text("Tập trung", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color(0xFF5D4037),
        indicatorColor = Color(0xFFFFB3A0) // Peach
      ),
      modifier = Modifier.testTag("nav_btn_focus")
    )
    NavigationBarItem(
      selected = currentTab == "pet",
      onClick = { onTabSelected("pet") },
      icon = { Icon(imageVector = Icons.Default.Pets, contentDescription = "Companion") },
      label = { Text("Thú cưng", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color(0xFF5D4037),
        indicatorColor = Color(0xFFFFB3A0) // Peach
      ),
      modifier = Modifier.testTag("nav_btn_pet")
    )
    NavigationBarItem(
      selected = currentTab == "profile",
      onClick = { onTabSelected("profile") },
      icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Stats") },
      label = { Text("Cá nhân", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color(0xFF5D4037),
        indicatorColor = Color(0xFFFFB3A0) // Peach
      ),
      modifier = Modifier.testTag("nav_btn_profile")
    )
  }
}
