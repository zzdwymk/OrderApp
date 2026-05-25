package com.zzdwymk.order.ui.page

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.launch
import kotlin.math.min

private data class NotificationMessage(
    val title: String,
    val content: String,
    val time: String,
    val icon: ImageVector,
    val unreadCount: Int,
    val section: String
)

private val sampleMessages = listOf(
    NotificationMessage("系统通知", "您的会员即将到期，续费享8折优惠", "刚刚", Icons.Default.Campaign, 1, "今天"),
    NotificationMessage("店铺助手", "您收藏的「老长沙龙虾馆」上新了3道菜品", "10:32", Icons.Default.Store, 2, "今天"),
    NotificationMessage("外卖小哥 A-1", "您的餐点已经放在门口啦，请记得取餐哦！", "09:15", Icons.Default.ChatBubble, 0, "今天"),
    NotificationMessage("优惠促销", "全场满100减20，限时抢购中", "08:42", Icons.Default.LocalOffer, 0, "今天"),
    NotificationMessage("外卖小哥 A-2", "您的外卖正在配送中，预计20分钟送达", "昨天 18:30", Icons.Default.ChatBubble, 0, "昨天"),
    NotificationMessage("系统通知", "您的账号在另一台设备登录，如非本人操作请及时修改密码", "昨天 14:20", Icons.Default.Notifications, 0, "昨天"),
    NotificationMessage("外卖小哥 A-3", "已送达，祝您用餐愉快！", "昨天 12:05", Icons.Default.ChatBubble, 0, "昨天"),
    NotificationMessage("店铺助手", "您常点的「湘味小厨」正在直播，速来围观", "周二", Icons.Default.Store, 0, "更早"),
    NotificationMessage("外卖小哥 A-4", "订单已完成，期待您的评价", "周一", Icons.Default.ChatBubble, 0, "更早"),
    NotificationMessage("系统通知", "版本更新：新功能「桌面小组件」已上线", "周日", Icons.Default.Campaign, 0, "更早"),
    NotificationMessage("外卖小哥 A-5", "感谢您的五星好评，我们会继续努力！", "周六", Icons.Default.ChatBubble, 0, "更早"),
    NotificationMessage("店铺助手", "周末特惠：全场饮品第二杯半价", "上周五", Icons.Default.LocalOffer, 0, "更早")
)

@Composable
fun NotificationPage() {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val scrollState = rememberLazyListState()

    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }

    val scrollProgress by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 1f
            else min(1f, scrollState.firstVisibleItemScrollOffset.toFloat() / 300f)
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = scrollProgress,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scrollProgress"
    )

    val titleSize = (18f + (24f - 18f) * animatedProgress).sp

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var titleWidthDp by remember { mutableFloatStateOf(0f) }
    val horizontalPadding = 16.dp

    val targetCenterX = remember(screenWidth, titleWidthDp) {
        if (titleWidthDp > 0f) {
            (screenWidth / 2) - (titleWidthDp.dp / 2) - horizontalPadding
        } else 0.dp
    }

    val offsetX by animateDpAsState(
        targetValue = targetCenterX * animatedProgress,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "offsetX"
    )

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().layerBackdrop(backdrop),
            contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var currentSection = ""
            sampleMessages.forEach { message ->
                if (message.section != currentSection) {
                    currentSection = message.section
                    item(key = "section_$currentSection") {
                        SectionHeader(currentSection)
                    }
                }
                item(key = "${message.section}_${message.title}_${message.time}") {
                    NotificationCard(message)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)),
                    RoundedCornerShape(0.dp)
                )
                .drawBackdrop(
                    backdrop,
                    { RoundedCornerShape(0.dp) },
                    { vibrancy(); blur(8.dp.toPx()) },
                    onDrawSurface = { drawRect(colorScheme.primaryContainer.copy(alpha = 0.3f)) }
                )
                .statusBarsPadding()
                .height(48.dp)
        ) {
            Text(
                text = "消息中心",
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = horizontalPadding)
                    .offset { IntOffset(offsetX.roundToPx(), 0) }
                    .onGloballyPositioned { titleWidthDp = with(density) { it.size.width.toDp().value } }
            )
            Text(
                "全部标为已读",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
private fun NotificationCard(message: NotificationMessage) {
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val press = remember { Animatable(0f) }
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)
    val bouncySpec = spring<Float>(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow)
    val hasUnread = message.unreadCount > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = lerp(1f, 0.97f, press.value)
                scaleY = lerp(1f, 0.97f, press.value)
                translationY = lerp(0f, 3.dp.toPx(), press.value)
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (hasUnread) colorScheme.primaryContainer.copy(alpha = 0.25f)
                else colorScheme.surface.copy(alpha = 0.5f).compositeOver(Color.White.copy(alpha = 0.3f)),
                RoundedCornerShape(20.dp)
            )
            .border(
                1.dp,
                if (hasUnread) colorScheme.primary.copy(alpha = 0.15f)
                else colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .pointerInput(message.title) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    scope.launch { press.animateTo(1f, downSpec) }
                    waitForUpOrCancellation()
                    scope.launch { press.animateTo(0f, bouncySpec) }
                }
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (hasUnread) colorScheme.primary.copy(alpha = 0.12f)
                        else colorScheme.primaryContainer.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = message.icon,
                    contentDescription = null,
                    tint = if (hasUnread) colorScheme.primary
                    else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .offset(x = 18.dp, y = (-18).dp)
                            .background(colorScheme.error, CircleShape)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = message.title,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message.content,
                    fontSize = 13.sp,
                    color = if (hasUnread) colorScheme.onSurfaceVariant
                    else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = message.time,
                    fontSize = 11.sp,
                    color = if (hasUnread) colorScheme.primary
                    else colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                if (message.unreadCount > 0) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(colorScheme.error, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${message.unreadCount}",
                            color = colorScheme.onError,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
