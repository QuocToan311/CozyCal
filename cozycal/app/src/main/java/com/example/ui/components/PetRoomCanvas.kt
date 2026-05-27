package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun PetRoomCanvas(
    modifier: Modifier = Modifier,
    isSleeping: Boolean = false,
    activeAccessories: Set<String> = emptySet(),
    petType: String = "Cat",
    onPetTap: () -> Unit = {}
) {
    // Continuous swaying/breathing animations
    val infiniteTransition = rememberInfiniteTransition(label = "PetAnimation")
    
    val tailSway by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TailSway"
    )

    val breathingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathing"
    )

    // Sleep bubbles floating
    val sleepZOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "SleepZ"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onPetTap() }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f + 20f

        // --- SPECIFIC COAT COLORS FOR PETS ---
        val primaryColor = when (petType) {
            "Cat" -> Color(0xFFF4D3A6) // Warm caramel cream
            "Bunny" -> Color(0xFFFFE2E2) // Soft strawberry milk
            "Fox" -> Color(0xFFFF7A45) // Rich foxy orange
            "Bear" -> Color(0xFF8D6E63) // Deeper cozy brown
            "Penguin" -> Color(0xFF263238) // Strong charcoal slate
            "Hamster" -> Color(0xFFFFA726) // Bright honey amber
            else -> Color(0xFFF4D3A6)
        }

        val secondaryColor = when (petType) {
            "Cat" -> Color(0xFFFFF8EE)
            "Bunny" -> Color(0xFFFFFBFC)
            "Fox" -> Color(0xFFFFF4E6) // Bright chest/muzzle
            "Bear" -> Color(0xFFE7D5CB) // Softer lighter belly
            "Penguin" -> Color(0xFFF2F7FA) // Crisp white belly
            "Hamster" -> Color(0xFFFFF1D9) // Warm cream belly
            else -> Color(0xFFFFF8EE)
        }

        val outlineColor = when (petType) {
            "Cat" -> Color(0xFFB57E4D)
            "Bunny" -> Color(0xFFE09AA6)
            "Fox" -> Color(0xFFCC5E2F)
            "Bear" -> Color(0xFF6D4C41)
            "Penguin" -> Color(0xFF0F1C23)
            "Hamster" -> Color(0xFFE08A1C)
            else -> Color(0xFFB57E4D)
        }

        val blushColor = when (petType) {
            "Cat" -> Color(0x66FF8A80)
            "Bunny" -> Color(0x77FF8FA0)
            "Fox" -> Color(0x55FF9A7A)
            "Bear" -> Color(0x55E7A17A)
            "Penguin" -> Color(0x44A7D8F0)
            "Hamster" -> Color(0x77FFB74D)
            else -> Color(0x66FF8A80)
        }

        // 1. Draw Lamp light beam if lamp is active
        if (activeAccessories.contains("lamp")) {
            val lampSourceX = width * 0.15f
            val lampSourceY = height * 0.25f
            val path = Path().apply {
                moveTo(lampSourceX, lampSourceY)
                lineTo(centerX - 130f, height)
                lineTo(centerX + 320f, height)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66FFEB3B), Color(0x00FFEB3B)),
                    center = Offset(lampSourceX, lampSourceY),
                    radius = width * 0.85f
                )
            )
        }

        // 2. Draw Woolen Rug under Mochi
        if (activeAccessories.contains("rug")) {
            drawOval(
                color = Color(0xFFFFCCBC), // Light peach rug
                topLeft = Offset(centerX - 160f, centerY + 30f),
                size = Size(320f, 90f)
            )
            // Rug decorative stripes
            drawOval(
                color = Color(0xFFFFAB91),
                topLeft = Offset(centerX - 130f, centerY + 43f),
                size = Size(260f, 64f),
                style = Stroke(width = 3f)
            )
        } else {
            // Tiny default floor shadow
            drawOval(
                color = Color(0x1F3D3A36),
                topLeft = Offset(centerX - 90f, centerY + 45f),
                size = Size(180f, 40f)
            )
        }

        // 3. Draw Floor Pillow
        if (activeAccessories.contains("pillow")) {
            drawRoundRect(
                color = Color(0xFFD1C4E9), // Gentle Lavender pillow
                topLeft = Offset(centerX + 80f, centerY + 10f),
                size = Size(100f, 55f),
                cornerRadius = CornerRadius(18f, 18f)
            )
            drawCircle(
                color = Color(0xFF9575CD),
                radius = 7f,
                center = Offset(centerX + 130f, centerY + 37f)
            )
        }

        // 4. Draw Monstera Plant
        if (activeAccessories.contains("plant")) {
            val potX = width * 0.82f
            val potY = centerY - 10f
            // Pot
            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(potX, potY),
                size = Size(60f, 70f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            // Leaves
            drawCircle(Color(0xFF81C784), radius = 23f, center = Offset(potX + 8f, potY - 18f))
            drawCircle(Color(0xFF4CAF50), radius = 26f, center = Offset(potX + 30f, potY - 32f))
            drawCircle(Color(0xFF388E3C), radius = 20f, center = Offset(potX + 52f, potY - 12f))
        }

        // 5. Draw Standing Lamp
        if (activeAccessories.contains("lamp")) {
            val lampX = width * 0.15f
            val lampY = height * 0.25f
            // Tall stand pole
            drawLine(
                color = Color(0xFF78909C),
                start = Offset(lampX, height * 0.85f),
                end = Offset(lampX, lampY),
                strokeWidth = 7f
            )
            // Shade
            val shadePath = Path().apply {
                moveTo(lampX - 32f, lampY + 14f)
                lineTo(lampX + 32f, lampY + 14f)
                lineTo(lampX + 18f, lampY - 22f)
                lineTo(lampX - 18f, lampY - 22f)
                close()
            }
            drawPath(shadePath, color = Color(0xFFFFB74D))
            // Stand base
            drawOval(
                color = Color(0xFF546E7A),
                topLeft = Offset(lampX - 25f, height * 0.82f),
                size = Size(50f, 18f)
            )
        }

        // 6. Draw Pet Tail (Customized per Type)
        val tailX = centerX - 55f
        val tailY = centerY + 32f

        when (petType) {
            "Cat", "Fox" -> {
                // Sleek swaying kitty / foxy tail
                val tailPath = Path().apply {
                    moveTo(tailX, tailY)
                    quadraticTo(
                        tailX - 45f + tailSway, tailY - 25f,
                        tailX - 25f + tailSway * 1.5f, tailY - 50f
                    )
                }
                // Outline & Main color
                drawPath(
                    path = tailPath,
                    color = primaryColor,
                    style = Stroke(width = 16f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                if (petType == "Fox") {
                    // WhiteTip at tail tip
                    drawCircle(
                        color = Color.White,
                        radius = 10f,
                        center = Offset(tailX - 25f + tailSway * 1.5f, tailY - 50f)
                    )
                }
            }
            "Bunny" -> {
                // Fluffy round bunny ball tail
                drawCircle(
                    color = Color.White,
                    radius = 15f,
                    center = Offset(centerX - 70f, centerY + 28f + breathingOffset)
                )
            }
            "Bear" -> {
                // Chubby bear tail
                drawCircle(
                    color = primaryColor,
                    radius = 16f,
                    center = Offset(centerX - 70f, centerY + 28f + breathingOffset)
                )
            }
            "Hamster" -> {
                // Hamster tiny pink tail stump
                drawCircle(
                    color = Color(0xFFFFCDD2),
                    radius = 8f,
                    center = Offset(centerX - 65f, centerY + 30f + breathingOffset)
                )
            }
            "Penguin" -> {
                // Small charcoal flipper tails / side wings
                drawOval(
                    color = primaryColor,
                    topLeft = Offset(centerX - 92f, centerY + 5f + breathingOffset),
                    size = Size(20f, 40f)
                )
                drawOval(
                    color = primaryColor,
                    topLeft = Offset(centerX + 72f, centerY + 5f + breathingOffset),
                    size = Size(20f, 40f)
                )
            }
        }

        // 7. Draw Pet Body
        drawOval(
            color = outlineColor.copy(alpha = 0.15f),
            topLeft = Offset(centerX - 83f, centerY - 28f + breathingOffset),
            size = Size(156f, 91f)
        )
        drawOval(
            color = primaryColor,
            topLeft = Offset(centerX - 80f, centerY - 25f + breathingOffset),
            size = Size(150f, 85f)
        )
        
        // Inside/Belly accents (such as Penguin's white chest or Hamster's belly)
        if (petType == "Penguin" || petType == "Hamster" || petType == "Fox") {
            drawOval(
                color = secondaryColor,
                topLeft = Offset(centerX - 50f, centerY - 15f + breathingOffset),
                size = Size(90f, 65f)
            )
        }

        // 8. Draw Pet Head
        val headY = centerY - 52f + breathingOffset
        drawCircle(
            color = outlineColor.copy(alpha = 0.15f),
            radius = 53f,
            center = Offset(centerX, headY - 1f)
        )
        drawCircle(
            color = primaryColor,
            radius = 50f,
            center = Offset(centerX, headY)
        )

        // Draw Ears based on Species
        when (petType) {
            "Cat" -> {
                val leftEar = Path().apply {
                    moveTo(centerX - 38f, headY - 25f)
                    lineTo(centerX - 48f, headY - 65f)
                    lineTo(centerX - 12f, headY - 45f)
                    close()
                }
                val rightEar = Path().apply {
                    moveTo(centerX + 38f, headY - 25f)
                    lineTo(centerX + 48f, headY - 65f)
                    lineTo(centerX + 12f, headY - 45f)
                    close()
                }
                drawPath(leftEar, outlineColor.copy(alpha = 0.18f))
                drawPath(rightEar, outlineColor.copy(alpha = 0.18f))
                drawPath(leftEar, primaryColor)
                drawPath(rightEar, primaryColor)
                
                // Pink inner ears
                drawCircle(Color(0xFFFFAAB4), radius = 8f, center = Offset(centerX - 35f, headY - 38f))
                drawCircle(Color(0xFFFFAAB4), radius = 8f, center = Offset(centerX + 35f, headY - 38f))
            }
            "Bunny" -> {
                // Tall upright Bunny Ears
                drawRoundRect(
                    color = outlineColor.copy(alpha = 0.18f),
                    topLeft = Offset(centerX - 37f, headY - 97f),
                    size = Size(28f, 64f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = outlineColor.copy(alpha = 0.18f),
                    topLeft = Offset(centerX + 9f, headY - 97f),
                    size = Size(28f, 64f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(centerX - 35f, headY - 95f),
                    size = Size(24f, 60f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(centerX + 11f, headY - 95f),
                    size = Size(24f, 60f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Bunny inner pink ears
                drawRoundRect(
                    color = Color(0xFFFFAAB4),
                    topLeft = Offset(centerX - 30f, headY - 88f),
                    size = Size(14f, 44f),
                    cornerRadius = CornerRadius(7f, 7f)
                )
                drawRoundRect(
                    color = Color(0xFFFFAAB4),
                    topLeft = Offset(centerX + 16f, headY - 88f),
                    size = Size(14f, 44f),
                    cornerRadius = CornerRadius(7f, 7f)
                )
            }
            "Fox" -> {
                // Fox pointy wild ears
                val leftEarFox = Path().apply {
                    moveTo(centerX - 40f, headY - 25f)
                    lineTo(centerX - 52f, headY - 70f)
                    lineTo(centerX - 12f, headY - 45f)
                    close()
                }
                val rightEarFox = Path().apply {
                    moveTo(centerX + 40f, headY - 25f)
                    lineTo(centerX + 52f, headY - 70f)
                    lineTo(centerX + 12f, headY - 45f)
                    close()
                }
                drawPath(leftEarFox, outlineColor.copy(alpha = 0.18f))
                drawPath(rightEarFox, outlineColor.copy(alpha = 0.18f))
                drawPath(leftEarFox, primaryColor)
                drawPath(rightEarFox, primaryColor)

                // Dark Fox Tips
                drawCircle(outlineColor, radius = 10f, center = Offset(centerX - 48f, headY - 64f))
                drawCircle(outlineColor, radius = 10f, center = Offset(centerX + 48f, headY - 64f))

                // Inner cream/white
                drawCircle(Color(0xFFFFF1DC), radius = 8f, center = Offset(centerX - 32f, headY - 36f))
                drawCircle(Color(0xFFFFF1DC), radius = 8f, center = Offset(centerX + 32f, headY - 36f))
            }
            "Bear" -> {
                // Chubby Brown Round Bear Ears
                drawCircle(outlineColor.copy(alpha = 0.18f), radius = 22f, center = Offset(centerX - 38f, headY - 36f))
                drawCircle(outlineColor.copy(alpha = 0.18f), radius = 22f, center = Offset(centerX + 38f, headY - 36f))
                drawCircle(primaryColor, radius = 20f, center = Offset(centerX - 38f, headY - 36f))
                drawCircle(primaryColor, radius = 20f, center = Offset(centerX + 38f, headY - 36f))
                // Inner ears cream
                drawCircle(Color(0xFFEBDDCF), radius = 11f, center = Offset(centerX - 38f, headY - 36f))
                drawCircle(Color(0xFFEBDDCF), radius = 11f, center = Offset(centerX + 38f, headY - 36f))
            }
            "Hamster" -> {
                // Tiny round pink/apricot ears
                drawCircle(outlineColor.copy(alpha = 0.18f), radius = 18f, center = Offset(centerX - 36f, headY - 38f))
                drawCircle(outlineColor.copy(alpha = 0.18f), radius = 18f, center = Offset(centerX + 36f, headY - 38f))
                drawCircle(primaryColor, radius = 16f, center = Offset(centerX - 36f, headY - 38f))
                drawCircle(primaryColor, radius = 16f, center = Offset(centerX + 36f, headY - 38f))
                drawCircle(Color(0xFFFFC08A), radius = 9f, center = Offset(centerX - 36f, headY - 38f))
                drawCircle(Color(0xFFFFC08A), radius = 9f, center = Offset(centerX + 36f, headY - 38f))
            }
            "Penguin" -> {
                // No external ears, maybe details for cute cozy headphones or earmuffs simulation? Just cute smooth penguin head!
            }
        }

        // White facial mask/eyes region for Fox
        if (petType == "Fox") {
            val leftMask = Path().apply {
                moveTo(centerX, headY + 12f)
                quadraticTo(centerX - 24f, headY - 6f, centerX - 42f, headY + 8f)
                quadraticTo(centerX - 30f, headY + 32f, centerX, headY + 18f)
                close()
            }
            val rightMask = Path().apply {
                moveTo(centerX, headY + 12f)
                quadraticTo(centerX + 24f, headY - 6f, centerX + 42f, headY + 8f)
                quadraticTo(centerX + 30f, headY + 32f, centerX, headY + 18f)
                close()
            }
            drawPath(leftMask, Color.White)
            drawPath(rightMask, Color.White)
        }

        // Muzzle Backgrounds (such as Bunny/Hamster white nose bridge or Bear's snout pad)
        if (petType == "Bear") {
            drawOval(
                color = Color(0xFFFFE0B2),
                topLeft = Offset(centerX - 20f, headY + 8f),
                size = Size(40f, 26f)
            )
        }

        // Rosy Blushing Cheeks
        drawCircle(blushColor, radius = 10f, center = Offset(centerX - 32f, headY + 12f))
        drawCircle(blushColor, radius = 10f, center = Offset(centerX + 32f, headY + 12f))

        // Eyes rendering
        if (isSleeping) {
            // Closed eyes
            drawLine(
                color = Color(0xFF3D2723),
                start = Offset(centerX - 28f, headY + 4f),
                end = Offset(centerX - 12f, headY + 4f),
                strokeWidth = 3.5f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF3D2723),
                start = Offset(centerX + 12f, headY + 4f),
                end = Offset(centerX + 28f, headY + 4f),
                strokeWidth = 3.5f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            // Draw floating cartoon sleeping 'Zzz' text bubbles
            drawSleepZText(centerX + 42f, headY - 35f + sleepZOffset, sleepZOffset)
        } else {
            // Shiny bead eyes (o-o)
            drawCircle(Color(0xFF3D3A36), radius = 5f, center = Offset(centerX - 20f, headY + 3f))
            drawCircle(Color(0xFF3D3A36), radius = 5f, center = Offset(centerX + 20f, headY + 3f))

            // Specular highlights
            drawCircle(Color.White, radius = 1.5f, center = Offset(centerX - 21.5f, headY + 1.5f))
            drawCircle(Color.White, radius = 1.5f, center = Offset(centerX + 18.5f, headY + 1.5f))
        }

        // Nose & Mouth based on Species
        when (petType) {
            "Cat" -> {
                // Smile cat mouth (w)
                val mouthPath = Path().apply {
                    moveTo(centerX - 7f, headY + 16f)
                    quadraticTo(centerX - 3.5f, headY + 21f, centerX, headY + 16f)
                    quadraticTo(centerX + 3.5f, headY + 21f, centerX + 7f, headY + 16f)
                }
                drawPath(
                    mouthPath,
                    color = Color(0xFF5D4037),
                    style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                // Kitty whiskers
                drawLine(Color(0xFFE0D8D0), start = Offset(centerX - 42f, headY + 12f), end = Offset(centerX - 62f, headY + 9f), strokeWidth = 2.5f)
                drawLine(Color(0xFFE0D8D0), start = Offset(centerX - 42f, headY + 18f), end = Offset(centerX - 62f, headY + 18f), strokeWidth = 2.5f)
                drawLine(Color(0xFFE0D8D0), start = Offset(centerX + 42f, headY + 12f), end = Offset(centerX + 62f, headY + 9f), strokeWidth = 2.5f)
                drawLine(Color(0xFFE0D8D0), start = Offset(centerX + 42f, headY + 18f), end = Offset(centerX + 62f, headY + 18f), strokeWidth = 2.5f)
            }
            "Bunny" -> {
                // Tiny bunny mouth (v) or (x)
                val mouthBunny = Path().apply {
                    moveTo(centerX - 4f, headY + 14f)
                    lineTo(centerX, headY + 17f)
                    lineTo(centerX + 4f, headY + 14f)
                    moveTo(centerX, headY + 17f)
                    lineTo(centerX, headY + 21f)
                }
                drawPath(mouthBunny, Color(0xFFFF8A80), style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            "Fox" -> {
                // Smart pointy smile
                val mouthFox = Path().apply {
                    moveTo(centerX - 6f, headY + 17f)
                    quadraticTo(centerX, headY + 22f, centerX + 6f, headY + 17f)
                }
                drawPath(mouthFox, Color(0xFF3E2723), style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            "Bear" -> {
                // Chubby cozy bear smile
                val mouthBear = Path().apply {
                    moveTo(centerX - 6f, headY + 18f)
                    quadraticTo(centerX - 3f, headY + 21f, centerX, headY + 18f)
                    quadraticTo(centerX + 3f, headY + 21f, centerX + 6f, headY + 18f)
                }
                drawPath(mouthBear, Color(0xFF3E2723), style = Stroke(width = 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            "Penguin" -> {
                // Cute bright orange triangular beak instead of standard mouth
                val beak = Path().apply {
                    moveTo(centerX - 10f, headY + 7f)
                    lineTo(centerX + 10f, headY + 7f)
                    lineTo(centerX, headY + 18f)
                    close()
                }
                drawPath(beak, Color(0xFFFFB300))
            }
            "Hamster" -> {
                // Super cute tiny hamster buck-teeth cheek line
                val mouthHamster = Path().apply {
                    moveTo(centerX - 4f, headY + 15f)
                    quadraticTo(centerX - 2f, headY + 18f, centerX, headY + 15f)
                    quadraticTo(centerX + 2f, headY + 18f, centerX + 4f, headY + 15f)
                }
                drawPath(mouthHamster, Color(0xFF5D4037), style = Stroke(width = 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
        }

        // Tiny cute pink nose (for non-penguins)
        if (petType != "Penguin") {
            val nose = Path().apply {
                moveTo(centerX - 3.5f, headY + 9f)
                lineTo(centerX + 3.5f, headY + 9f)
                lineTo(centerX, headY + 13f)
                close()
            }
            drawPath(nose, color = Color(0xFFFF8A80))
        }
    }
}

// Draw custom vector Zzz path on canvas
private fun DrawScope.drawSleepZText(x: Float, y: Float, offset: Float) {
    val sizeScale = (1.0f - (kotlin.math.abs(offset) / 35.0f)).coerceIn(0.2f, 1.0f)
    val color = Color(0xFF9575CD).copy(alpha = sizeScale)
    
    val zPath = Path().apply {
        moveTo(x - 10f * sizeScale, y - 8f * sizeScale)
        lineTo(x + 10f * sizeScale, y - 8f * sizeScale)
        lineTo(x - 10f * sizeScale, y + 8f * sizeScale)
        lineTo(x + 10f * sizeScale, y + 8f * sizeScale)
    }
    
    drawPath(
        path = zPath,
        color = color,
        style = Stroke(
            width = 4f * sizeScale,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )
    )
}
