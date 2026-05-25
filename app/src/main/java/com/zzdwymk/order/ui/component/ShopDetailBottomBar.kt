package com.zzdwymk.order.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import com.zzdwymk.order.ui.HapticUtils

@Preview(showBackground = true)
@Composable
fun ShopDetailFluidBottomBarPreview() {
    val colorScheme = MaterialTheme.colorScheme
    val backdrop = com.kyant.backdrop.backdrops.rememberLayerBackdrop { }

    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.background)
                .padding(vertical = 16.dp)
        ) {
            ShopDetailFluidBottomBar(
                backdrop = backdrop,
                totalCount = 3,
                totalPrice = 45.5,
                onCartClick = {},
                onOrderClick = {}
            )
        }
    }
}

@Composable
fun ShopDetailFluidBottomBar(
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    totalCount: Int,
    totalPrice: Double,
    onCartClick: () -> Unit,
    onOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val animationScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val barHeight = 84.dp
    val horizontalPadding = 12.dp
    val actionSize = 64.dp

    val cartBtnPress = remember { Animatable(0f) }
    val orderBtnPress = remember { Animatable(0f) }

    val bouncySpec = spring<Float>(dampingRatio = 0.2f, stiffness = Spring.StiffnessMediumLow)
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(barHeight)
            .padding(horizontal = horizontalPadding, vertical = 14.dp)
    ) {

        // 左侧购物车
        Box(
            modifier = Modifier
                .widthIn(min = 180.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    val p = cartBtnPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                }
                .drawBackdrop(backdrop, { CircleShape }, {
                    vibrancy()
                    blur(8.dp.toPx())
                })
                .background(
                    colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)),
                    CircleShape
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        )

        Box(
            modifier = Modifier
                .widthIn(min = 180.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .graphicsLayer {
                    val p = cartBtnPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        animationScope.launch { cartBtnPress.animateTo(1f, downSpec) }
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            HapticUtils.performMediumImpact(hapticFeedback)
                            onCartClick()
                        }
                        animationScope.launch { cartBtnPress.animateTo(0f, bouncySpec) }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(44.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart, null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp).align(Alignment.Center)
                        )
                    }
                    if (totalCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(18.dp)
                                .background(colorScheme.error, CircleShape)
                                .border(1.5.dp, colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (totalCount > 99) "99+" else "$totalCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onError,
                                textAlign = TextAlign.Center,
//                                modifier = Modifier.padding(top = 2.dp)
                                lineHeight = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        "已选 $totalCount 件",
                        fontSize = 9.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        "¥${String.format(LocalLocale.current.platformLocale, "%.1f", totalPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 右侧下单按钮
        Box(
            modifier = Modifier
                .size(actionSize)
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    val p = orderBtnPress.value
                    scaleX = lerp(1f, 0.9f, p)
                    scaleY = lerp(1f, 0.9f, p)
                }
                .drawBackdrop(backdrop, { CircleShape }, {
                    vibrancy()
                    blur(8.dp.toPx())
                })
                .background(
                    colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)),
                    CircleShape
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(actionSize)
                .align(Alignment.CenterEnd)
                .clip(CircleShape)
                .graphicsLayer {
                    val p = orderBtnPress.value
                    scaleX = lerp(1f, 0.9f, p)
                    scaleY = lerp(1f, 0.9f, p)
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        animationScope.launch { orderBtnPress.animateTo(1f, downSpec) }
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            HapticUtils.performMediumImpact(hapticFeedback)
                            onOrderClick()
                        }
                        animationScope.launch { orderBtnPress.animateTo(0f, bouncySpec) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "下单",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
        }
    }
}
