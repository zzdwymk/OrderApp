package com.zzdwymk.order.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun GlassOrderFramePreview(){
    val colorScheme = MaterialTheme.colorScheme
    MaterialTheme(colorScheme = colorScheme) {
        GlassOrderFrame(tintColor = colorScheme.primary) {
            Column {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "示例商家",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    androidx.compose.material3.Text(
                        text = "配送中",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.primary
                    )
                }
            }
        }
    }
}


@Composable
fun GlassOrderFrame(
    modifier: Modifier = Modifier,
    tintColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    renderEffect = androidx.compose.ui.graphics.BlurEffect(
                        radiusX = 40f,
                        radiusY = 40f,
                        edgeTreatment = androidx.compose.ui.graphics.TileMode.Decal
                    )
                    clip = true
                    shape = RoundedCornerShape(cornerRadius)
                }
                .background(
                    color = tintColor.copy(alpha = 0.15f).compositeOver(Color.White.copy(0.8f)),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(0.2f),
                    shape = RoundedCornerShape(cornerRadius)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            content()
        }
    }
}