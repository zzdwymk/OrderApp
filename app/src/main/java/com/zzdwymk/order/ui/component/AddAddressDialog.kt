package com.zzdwymk.order.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zzdwymk.order.model.Address

@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit,
    colorScheme: ColorScheme,
    editAddress: Address? = null
) {
    val isEditing = editAddress != null
    var inputMode by remember { mutableStateOf("map") }
    var name by remember { mutableStateOf(editAddress?.name ?: "") }
    var phone by remember { mutableStateOf(editAddress?.phone ?: "") }
    var detailAddress by remember { mutableStateOf(editAddress?.detailAddress ?: "") }
    var fullAddress by remember { mutableStateOf(editAddress?.fullAddress ?: "") }
    var isDefault by remember { mutableStateOf(editAddress?.isDefault ?: false) }
    var mapAddress by remember { mutableStateOf(if (editAddress != null) editAddress.fullAddress else "点击地图图标获取位置") }
    var latitude by remember { mutableStateOf(editAddress?.latitude ?: 0.0) }
    var longitude by remember { mutableStateOf(editAddress?.longitude ?: 0.0) }

    val isFormValid = name.isNotBlank() && phone.isNotBlank() && (detailAddress.isNotBlank() || mapAddress != "点击地图图标获取位置")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "编辑收货地址" else "新增收货地址",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModeButton(
                        text = "地图选点",
                        icon = Icons.Default.Map,
                        isSelected = inputMode == "map",
                        onClick = { inputMode = "map" },
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f)
                    )
                    ModeButton(
                        text = "手动输入",
                        icon = Icons.Default.Edit,
                        isSelected = inputMode == "manual",
                        onClick = { inputMode = "manual" },
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedVisibility(visible = inputMode == "map") {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.surface,
                            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "搜索地址...",
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.15f)),
                            onClick = {
                                mapAddress = "北京市朝阳区建国路88号SOHO现代城"
                                fullAddress = mapAddress
                                latitude = 39.908
                                longitude = 116.408
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    repeat(5) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .background(
                                                    if (it % 2 == 0) colorScheme.surfaceContainerHighest.copy(alpha = 0.2f)
                                                    else Color.Transparent
                                                )
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = colorScheme.error,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    if (mapAddress == "点击地图图标获取位置") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = colorScheme.primaryContainer.copy(alpha = 0.9f)
                                        ) {
                                            Text(
                                                text = "点击地图选点",
                                                fontSize = 12.sp,
                                                color = colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                if (mapAddress != "点击地图图标获取位置") {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = colorScheme.error,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = colorScheme.surface.copy(alpha = 0.95f),
                                                shadowElevation = 2.dp
                                            ) {
                                                Text(
                                                    text = mapAddress,
                                                    fontSize = 12.sp,
                                                    color = colorScheme.onSurface,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "拖动地图或点击选择位置",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Text(
                    text = "联系人信息",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    placeholder = { Text("请输入收货人姓名") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("手机号") },
                    placeholder = { Text("请输入收货人手机号") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "收货地址",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = if (inputMode == "map" && mapAddress != "点击地图图标获取位置") mapAddress else detailAddress,
                    onValueChange = {
                        detailAddress = it
                        if (inputMode == "manual") fullAddress = it
                    },
                    label = { Text("详细地址") },
                    placeholder = { Text("如：XX路XX号XX楼XX室") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isDefault) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "设为默认地址",
                            fontSize = 14.sp,
                            color = colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.primary,
                            checkedTrackColor = colorScheme.primaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val addrDetail = if (inputMode == "map" && mapAddress != "点击地图图标获取位置") {
                            if (detailAddress.isNotBlank()) "$mapAddress $detailAddress" else mapAddress
                        } else detailAddress
                        val address = Address(
                            id = editAddress?.id ?: "",
                            name = name,
                            phone = phone,
                            detailAddress = addrDetail,
                            fullAddress = fullAddress.ifBlank { addrDetail },
                            latitude = latitude,
                            longitude = longitude,
                            isDefault = isDefault
                        )
                        onSave(address)
                    },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        disabledContainerColor = colorScheme.surfaceContainerHighest
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "保存地址",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.3f)
                else colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        border = if (isSelected) BorderStroke(2.dp, colorScheme.primary.copy(alpha = 0.5f))
                 else BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
        onClick = onClick,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
            )
        }
    }
}
