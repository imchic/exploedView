package com.example.exploedview.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

@Composable
fun NeoText(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight,
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp // 현재 화면 너비 (dp)

    // 비율에 맞게끔 재 조정
    val adjustedFontSize = if (screenWidth < 600) {
        fontSize * 0.8f
    } else {
        fontSize
    }


    Text(
        text = text,
        style = TextStyle(
            fontSize = adjustedFontSize,
            color = Color.Black,
            fontWeight = fontWeight

        )
    )
}

