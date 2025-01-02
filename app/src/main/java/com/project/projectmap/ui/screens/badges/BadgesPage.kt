package com.project.projectmap.ui.screens.badges

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.projectmap.R
import com.project.projectmap.ui.viewModel.BadgesViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.abs

data class Item(val id: String, val name: String, val price: Int, val category: String)

@Composable
fun BadgesPage(
    onClose: () -> Unit = {},
) {
    val viewModel: BadgesViewModel = viewModel()
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf("Toys", "Foods", "Drinks")
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showInsufficientFundsMessage by viewModel.showInsufficientFundsMessage.collectAsState()

    val context = LocalContext.current

    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val badgeItems by viewModel.items.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var formattedPts by remember { mutableStateOf<String?>("") }
    val userPtsFloat = user?.profile?.coin?.toFloat() ?: 0f
    val floatFormat = DecimalFormat("#.#")

    user?.let { userInfo ->
        formattedPts = when (userInfo.profile.coin) {
            in 0..999 -> userInfo.profile.coin.toString()
            in 1000..999999 -> "${floatFormat.format(userPtsFloat / 1000)}k"
            in 1000000..999999999 -> "${floatFormat.format(userPtsFloat / 1000000)}M"
            in 1000000000..Int.MAX_VALUE -> "${floatFormat.format(userPtsFloat / 1000000000)}B"
            else -> "0"
        }
    }

    val itemWidth = 80.dp
    val itemSpacing = 16.dp
    val containerWidth = (itemWidth * 3) + (itemSpacing * 2)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                            text = formattedPts ?: "0",
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
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top)
            ) {
                Text(
                    text = "Badges",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Column {

                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.char_bunny),
                        contentDescription = "Bunny with carrot",
                        modifier = Modifier.size(200.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        text = badgeItems.firstOrNull { it.category == pages[currentPage] }?.category ?: "",
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
                        items(badgeItems.filter { it.category == pages[currentPage] }) { badgeItem ->
                            val isOwned = user?.items?.contains(badgeItem.id) == true
                            val isEquipped = user?.equippedItem == badgeItem.id

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(itemWidth)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                ) {
                                    Text(
                                        text = badgeItem.name,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${badgeItem.price} coins",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (!isOwned) {
                                            viewModel.buyItem(badgeItem)
                                        } else {
                                            viewModel.equipItem(badgeItem.id)
                                        }
                                    },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            !isOwned -> MaterialTheme.colorScheme.primary
                                            isEquipped -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.secondary
                                        }
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when {
                                            !isOwned -> "Buy"
                                            isEquipped -> "Used"
                                            else -> "Use"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            !isOwned || isEquipped -> Color.White
                                            else -> MaterialTheme.colorScheme.onSecondary
                                        }
                                    )
                                }
                            }
                        }
                    }

                    LaunchedEffect(lazyListState.isScrollInProgress) {
                        if (!lazyListState.isScrollInProgress) {
                            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                            if (visibleItems.isNotEmpty()) {
                                val firstVisibleItem = visibleItems.first()
                                val offset = firstVisibleItem.offset.toFloat()
                                val size = firstVisibleItem.size.toFloat()

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

                if (user?.equippedItem != null) {
                    Text(
                        text = "Equipped: ${user.equippedItem}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        LaunchedEffect(showInsufficientFundsMessage) {
            if (showInsufficientFundsMessage) {
                Toast.makeText(context, "Insufficient funds to buy the item", Toast.LENGTH_SHORT).show()
            }
        }
    }


}