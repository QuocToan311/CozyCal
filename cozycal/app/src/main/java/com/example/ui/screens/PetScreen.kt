package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Storefront
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
import com.example.data.model.PetState
import com.example.ui.components.PetRoomCanvas
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PetScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val petState by viewModel.petState.collectAsState()

    var petMeowBubbleStr by remember { mutableStateOf("Meow! Tớ là Mochi. Chào mừng tới phòng của tớ! 😽") }

    val shopItems = remember {
        listOf(
            ShopItem(
                id = "rug",
                name = "Thảm Đào Ấm Áp 🍑",
                cost = 100,
                desc = "Thảm len tròn ấm áp trải dưới chân Mochi"
            ),
            ShopItem(
                id = "lamp",
                name = "Đèn Bàn Vàng Ambient 💡",
                cost = 150,
                desc = "Đèn tạo ánh sáng vàng ấm áp xiên chéo căn phòng"
            ),
            ShopItem(
                id = "plant",
                name = "Chậu Trồng Monstera 🌿",
                cost = 200,
                desc = "Bổ sung không khí thiên nhiên mát lành cho phòng"
            ),
            ShopItem(
                id = "pillow",
                name = "Gối Tựa Lavender 🛋️",
                cost = 250,
                desc = "Gối bông xinh xắn cho Mochi dựa ngủ"
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("pet_screen_column")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title Area
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Góc Thú Cưng Virtual",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Người bạn đồng hành tự chăm sóc bản thân của bạn",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 2. Interactive Pet Sanctuary & Speech bubble
        item {
            petState?.let { pet ->
                val accessoriesSet = remember(pet.activeAccessories) {
                    pet.activeAccessories.split(",").toSet()
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .testTag("pet_fullscreen_room"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Drawing room, decorations, and cat Mochi
                        PetRoomCanvas(
                            isSleeping = viewModel.isFocusTimerRunning,
                            activeAccessories = accessoriesSet,
                            petType = pet.petType,
                            onPetTap = {
                                val quotes = when (pet.petType) {
                                    "Bunny" -> listOf(
                                        "Pi pi... Bạn đã nuôi dưỡng thói quen của mình chưa thế? 🥕",
                                        "Tai tớ hôm nay vểnh lên vì thấy bạn cực kỳ chăm chỉ! 🐰",
                                        "Chu chu... Tớ thích nhảy nhót quanh căn phòng thơm tho này!",
                                        "Nhớ nghỉ tay một chút nhé, tớ chuẩn bị cỏ ba lá may mắn cho bạn nè!"
                                    )
                                    "Fox" -> listOf(
                                        "Cáo nhỏ thông minh chúc bạn có một ngày hoàn thành mục tiêu tốt lành! 🦊",
                                        "Đuôi tớ xòe to mỗi khi bạn hoàn thành một sự kiện trên lịch!",
                                        "Tìm thấy sự bình yên ở ngày bận rộn... Cùng tớ hít thở an nhiên!",
                                        "Bạn là người bạn tâm giao tuyệt vời nhất của tớ!"
                                    )
                                    "Bear" -> listOf(
                                        "Gấu nâu cozy thích ngủ nướng và nhìn bạn làm việc chăm chỉ... 🐻",
                                        "Grrr... Hãy ôm tớ thật chặt khi bạn cảm thấy áp lực nha!",
                                        "Một cốc mật ong sữa ấm cho buổi tập trung dịu mát nhé!",
                                        "Bạn đang tiến bộ từng ngày đấy, tớ tự hào vô cùng!"
                                    )
                                    "Penguin" -> listOf(
                                        "Pingu! Hôm nay thời tiết thật mát mẻ và sảng khoái! 🐧",
                                        "Đập hai cánh bành bạch... Cùng tớ tập trung 25 phút nha!",
                                        "Chúng mình giống như hai nhà thám hiểm cuộc sống an lành!",
                                        "Nhìn căn phòng xinh xắn của chúng mình gọn gàng chưa kìa!"
                                    )
                                    "Hamster" -> listOf(
                                        "Chít chít... Má tớ đang ngậm đầy hạt dẻ may mắn cho bạn! 🐹",
                                        "Quẹt quẹt râu... Chạy bánh xe thói quen thật là vui vẻ!",
                                        "Bạn nhỏ nhắn nhưng chứa đựng sức mạnh tập trung khổng lồ!",
                                        "Cố gắng lên nhé, tớ luôn lăn tròn cổ vũ bạn đấy!"
                                    )
                                    else -> listOf(
                                        "Meow... Hôm nay bạn uống đủ cốc nước ấm chưa thế? 🥤",
                                        "Khò khò... Bạn làm việc chăm chỉ thế này tớ thích lắm! 🧸",
                                        "Purr... Vuốt ve tớ ấm sực luôn! Gặp bạn tớ vui lắm!",
                                        "Meow! Đừng quên nghỉ ngơi sau mỗi 25p nha!",
                                        "Chụt chụt! Bạn là người thông thái nhất thế giới luôn!",
                                        "Phòng tớ có đồ chơi mới rồi! Cảm ơn bạn nhiều nhé ~ ❤️"
                                    )
                                }
                                petMeowBubbleStr = quotes.random()
                            }
                        )

                        // Float Speech Bubble
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                                .fillMaxWidth(0.85f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.95f))
                                    .border(1.dp, Color(0xFFFFB3A0).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = petMeowBubbleStr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Bottom-Left Status indicators overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "Mood", tint = Color(0xFFFF8A75), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mood: ${pet.mood}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Cấp: ${pet.level} (XP: ${pet.exp}/${pet.level * 100})", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                        }

                        // Bottom-Right Coin display
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF7CE))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFBC02D), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${pet.coins} Xu", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                    }
                }
            }
        }

        // 3. Shop & Setup Decoration Heading
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = "Shop",
                    tint = Color(0xFFFFB3A0),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cửa Hàng Quà Tặng Cozy Room",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }

        // List shop items
        items(shopItems) { shopItem ->
            petState?.let { pet ->
                val unlockedAccessories = remember(pet.unlockedAccessories) {
                    pet.unlockedAccessories.split(",").toSet()
                }
                val activeAccessories = remember(pet.activeAccessories) {
                    pet.activeAccessories.split(",").toSet()
                }

                val isUnlocked = unlockedAccessories.contains(shopItem.id)
                val isActive = activeAccessories.contains(shopItem.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_item_${shopItem.id}"),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shopItem.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = shopItem.desc,
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 12.sp
                            )
                        }

                        // Buy button / Equip toggle
                        Button(
                            onClick = {
                                if (isUnlocked) {
                                    viewModel.toggleAccessoryActive(shopItem.id)
                                } else {
                                    viewModel.buyAccessory(shopItem.id, shopItem.cost)
                                }
                            },
                            modifier = Modifier.testTag("buy_equip_btn_${shopItem.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isActive -> Color(0xFFCFEADF) // Active Mint
                                    isUnlocked -> Color(0xFFE5E3F7) // Equppable Lavender
                                    else -> Color(0xFFFFB3A0) // Buy Peach
                                }
                            )
                        ) {
                            Text(
                                text = when {
                                    isActive -> "Đang Treo"
                                    isUnlocked -> "Treo Lên"
                                    else -> "${shopItem.cost} Xu"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) TextDark else if (isUnlocked) TextDark else Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pet_unlocked_tip"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CreamBackground)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tip",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tips: Hoàn thành thêm các event trong Calendar và các thói quen Habit để có thêm Xu sắm đồ cho Mochi nhé!",
                        fontSize = 10.sp,
                        color = TextMuted,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class ShopItem(
    val id: String,
    val name: String,
    val cost: Int,
    val desc: String
)
