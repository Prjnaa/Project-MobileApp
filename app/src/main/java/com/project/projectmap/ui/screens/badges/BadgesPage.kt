package com.project.projectmap.ui.screens.badges

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.projectmap.R
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun BadgesPage(
    onClose: () -> Unit = {}
) {
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf("Toys", "Foods", "Drinks")
    val badges = remember { List(10) { it } }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Calculate exact widths for 3-item container
    val itemWidth = 80.dp
    val itemSpacing = 16.dp
    // Total width = 3 items + 2 spaces between them
    val containerWidth = (itemWidth * 3) + (itemSpacing * 2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, top = 54.dp, end = 16.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coins Display aligned to left corner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .height(54.dp)
                    .widthIn(max = 120.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.coin_24),
                        contentDescription = "Coin Icon",
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "250",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = { onClose() },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close Icon",
                    modifier = Modifier
                        .size(24.dp)
                )
            }

        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top)
        ) {
            // Badges Title
            Text(
                text = "Badges",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Column {

            }
            // Main Bunny Image
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.char_bunny),
                    contentDescription = "Bunny with carrot",
                    modifier = Modifier.size(200.dp)
                )
            }

            // Category Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "Previous",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (currentPage > 0) currentPage--
                        }
                )

                Text(
                    text = pages[currentPage],
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(120.dp)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (currentPage < pages.size - 1) currentPage++
                        }
                )
            }

            // Badge Slots with Fixed Width Container
            Box(
                modifier = Modifier
                    .width(containerWidth)
                    .padding(vertical = 24.dp)
            ) {
                LazyRow(
                    state = lazyListState,
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(badges) { badge ->
                        Box(
                            modifier = Modifier
                                .size(itemWidth)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    }
                }

                // Subtle snap behavior for "nanggung" positions
                LaunchedEffect(lazyListState.isScrollInProgress) {
                    if (!lazyListState.isScrollInProgress) {
                        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                        if (visibleItems.isNotEmpty()) {
                            val firstVisibleItem = visibleItems.first()
                            val offset = firstVisibleItem.offset.toFloat()
                            val size = firstVisibleItem.size.toFloat()

                            // Only adjust if the item is significantly offset (nanggung)
                            // But not too far scrolled (to prevent auto-scrolling to next)
                            if (abs(offset) > size * 0.2f && abs(offset) < size * 0.8f) {
                                val targetIndex = if (offset < 0) {
                                    firstVisibleItem.index + 1
                                } else {
                                    firstVisibleItem.index
                                }

                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(targetIndex)
                                }
                            }
                        }
                    }
                }
            }

            // Wear This Button
            Button(
                onClick = { /* Implement wear functionality */ },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Wear This",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}