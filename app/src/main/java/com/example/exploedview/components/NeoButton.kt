package com.example.exploedview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier,
) {
    val gradientBrush = if (enabled) {
//        Brush.linearGradient(
//            colors = listOf(
//                Color(0xff5038ED), Color(0xff9181F4)
//            )
//        )
        Brush.horizontalGradient(listOf(Color(0xff5038ED), Color(0xff9181F4)))
    } else {
        Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)) // 비활성화 상태
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color.Transparent,
            disabledContentColor = Color.Gray,
            disabledContainerColor = Color.Transparent
        ),
        modifier = modifier.background(brush = gradientBrush, shape = RoundedCornerShape(16.dp)),
    ) {
        Box {
            Text(
                text = text,
                color = Color.White,
            )
        }
    }

}