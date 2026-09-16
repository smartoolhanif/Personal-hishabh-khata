package com.example.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val badge: String
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val steps = listOf(
        OnboardingStep(
            title = "স্মার্ট হিসাব-খাতা",
            subtitle = "সহজ আয় ও ব্যয়ের ডিজিটাল খাতা",
            description = "দৈনন্দিন আয় ও ব্যয়ের পুঙ্খানুপুঙ্খ হিসাব রাখুন। ক্যাটাগরি, তারিখ ও ভাউচার অনুযায়ী তাৎক্ষণিক হিসাব সংরক্ষণ করুন।",
            icon = Icons.Filled.AttachMoney,
            badge = "দৈনিক লেজার"
        ),
        OnboardingStep(
            title = "বাকি ও ধার খাতা",
            subtitle = "কার কাছে কত পাবেন বা আপনি কত দেবেন",
            description = "গ্রাহক ও মহাজনের বাকি খাতা আলাদা রাখুন। কার কত বকেয়া আছে, পরিশোধের তারিখ ও হিস্ট্রি এক নজরে ট্র্যাক করুন।",
            icon = Icons.Filled.MenuBook,
            badge = "কাস্টমার খাতা"
        ),
        OnboardingStep(
            title = "মাল্টি-ওয়ালেট ও বাজেট",
            subtitle = "নগদ, ব্যাংক, বিকাশ ও ক্যাটাগরি বাজেট",
            description = "নগদ টাকা, ব্যাংক বা বিকাশের আলাদা ব্যালেন্স দেখুন। ক্যাটাগরি অনুযায়ী মাসিক বাজেট সেট করে অতিরিক্ত খরচ রোধ করুন।",
            icon = Icons.Filled.AccountBalanceWallet,
            badge = "একাধিক অ্যাকাউন্ট"
        ),
        OnboardingStep(
            title = "রসিদ ও PDF রিপোর্ট",
            subtitle = "ছবি যোগ ও এক ক্লিকে এক্সপোর্ট",
            description = "লেনদেনের সাথে রসিদ বা ভাউচারের ছবি তুলে রাখুন। যে কোনো সময় সম্পূর্ণ হিসাবের PDF বা Excel রিপোর্ট শেয়ার করুন।",
            icon = Icons.Filled.PictureAsPdf,
            badge = "রিপোর্ট এক্সপোর্ট"
        )
    )

    val pagerState = rememberPagerState { steps.size }
    val scope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinished) {
                    Text(
                        text = "এড়িয়ে যান (Skip)",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val step = steps[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Feature Icon inside stylish gradient halo
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = step.badge,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Bottom Indicators and Navigation Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(steps.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val isLastPage = pagerState.currentPage == steps.size - 1

                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinished()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLastPage) "শুরু করুন (Get Started)" else "পরবর্তী (Next)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isLastPage) Icons.Filled.Check else Icons.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
