package com.example.exploedview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable() (() -> Unit)? = null,
    isError: Boolean = false,
    isErrorText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {

    if (isError) {
        Column {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                isErrorText ?: "Error",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
//        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
        ),
        visualTransformation = visualTransformation,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        textStyle = TextStyle(fontSize = 16.sp), // 텍스트 크기 설정
        modifier = Modifier
            .padding(start = 0.dp, end = 0.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth()
            .background(
                color = Color(0xCCF0EDFF), shape = RoundedCornerShape(size = 16.dp)
            )
    )

}