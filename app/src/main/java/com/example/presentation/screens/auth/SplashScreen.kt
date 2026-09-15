package com.example.presentation.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToBiometricLock: () -> Unit
) {
    val context = LocalContext.current
    val user = viewModel.currentUser.collectAsState().value

    // Animation States for the 5-step sequence
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.85f) }
    val scanBeamY = remember { Animatable(0.15f) }
    val scanBeamAlpha = remember { Animatable(0f) }
    val flashGlowAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // STEP 1: Screen opens with branded deep navy gradient background
        delay(100)

        // STEP 2: Flash Scan icon smoothly fades and scales in
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(1f, animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing))
        }
        launch {
            delay(200)
            textAlpha.animateTo(1f, animationSpec = tween(durationMillis = 350))
        }

        delay(400)

        // STEP 3: Scanner laser light moves smoothly across the document icon
        scanBeamAlpha.animateTo(1f, animationSpec = tween(150))
        launch {
            // Sweep down
            scanBeamY.animateTo(0.85f, animationSpec = tween(durationMillis = 600, easing = LinearEasing))
            // Sweep back up smoothly
            scanBeamY.animateTo(0.45f, animationSpec = tween(durationMillis = 350, easing = LinearEasing))
        }

        delay(850)

        // STEP 4: Subtle flash / glow effect pulse
        launch {
            flashGlowAlpha.animateTo(1f, animationSpec = tween(durationMillis = 150))
            flashGlowAlpha.animateTo(0f, animationSpec = tween(durationMillis = 280))
        }
        scanBeamAlpha.animateTo(0f, animationSpec = tween(200))

        // Wait for final polish before transition
        delay(400)

        // STEP 5: Smoothly transition to next screen
        if (user != null) {
            val isBioAvailable = viewModel.isBiometricAvailable(context)
            val isBioEnabled = viewModel.isBiometricEnabledForUser(user.uid)
            val isSessionUnlocked = viewModel.sessionLockManager.isUserSessionUnlocked(user.uid)

            if (isBioAvailable && isBioEnabled && !isSessionUnlocked) {
                onNavigateToBiometricLock()
            } else {
                viewModel.unlockSession(user.uid)
                onNavigateToHome()
            }
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1B36),
                        Color(0xFF071426),
                        Color(0xFF030914)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle background technology radial glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x2200E5FF),
                        Color(0x0A00E5FF),
                        Color.Transparent
                    ),
                    center = canvasCenter,
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = canvasCenter
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Flash Scan Central Logo Box
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Outer Cyan Glow Aura
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0xFF00E5FF),
                            ambientColor = Color(0xFF00E5FF)
                        )
                )

                // Standalone Flash Scan Official Vector Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_flash_scan_logo),
                    contentDescription = "Flash Scan Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(26.dp))
                )

                // Scanning Beam Laser Overlay
                if (scanBeamAlpha.value > 0f) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(26.dp))
                    ) {
                        val currentY = size.height * scanBeamY.value
                        val beamHeight = 12f

                        // Broad Soft Glow
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x6600E5FF).copy(alpha = scanBeamAlpha.value * 0.6f),
                                    Color.Transparent
                                ),
                                startY = currentY - beamHeight * 3,
                                endY = currentY + beamHeight * 3
                            ),
                            topLeft = Offset(0f, currentY - beamHeight * 3),
                            size = Size(size.width, beamHeight * 6)
                        )

                        // Intense Core Laser Line
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF00E5FF).copy(alpha = scanBeamAlpha.value),
                                    Color.White.copy(alpha = scanBeamAlpha.value),
                                    Color(0xFF00E5FF).copy(alpha = scanBeamAlpha.value),
                                    Color.Transparent
                                )
                            ),
                            topLeft = Offset(14f, currentY - 2f),
                            size = Size(size.width - 28f, 4f)
                        )
                    }
                }

                // Flash Burst Sparkle Glow (Step 4)
                if (flashGlowAlpha.value > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(flashGlowAlpha.value)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xD0FFFFFF),
                                        Color(0x8000E5FF),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Typography
            Row(
                modifier = Modifier.alpha(textAlpha.value),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FLASH",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Text(
                    text = " SCAN",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SMART DOCUMENT SCANNER",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8BA6C9),
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        // Bottom Loading Dots & Technology Badge
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(textAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E5FF)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = 0.5f)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = 0.25f)))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "AI SCAN ENGINE ACTIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5A7B9D),
                letterSpacing = 2.sp
            )
        }
    }
}
