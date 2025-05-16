import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import com.example.exploedview.components.NeoTextField
import com.example.exploedview.util.LogUtil

@Composable
fun ForgotPasswordScreen(onDismiss: () -> Unit, onConfirm: () -> Unit, isFindPassword: Boolean) {

    val email by remember { mutableStateOf("") }

    if (isFindPassword) {
        Column {
            FindPwDialog(
                email = email,
                onDismiss = { onDismiss() },
                onConfirm = onConfirm
            )
        }
    }

}

@Composable
fun FindPwDialog(email: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {

    var updateEmail by remember { mutableStateOf(email) }
    var isEmailError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("비밀번호 찾기") },
        text = {
            Column {
                NeoTextField(
                    value = updateEmail,
                    onValueChange = {
                        updateEmail = it
                        isEmailError = validateEmail(it) != null
                    },
                    labelText = "이메일",

                    isErrorText = "이메일 형식이 올바르지 않습니다.",
                    visualTransformation = VisualTransformation.None,
                    modifier = Modifier.Companion.fillMaxWidth()

                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("전송")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

fun validateEmail(email: String): String? {
    LogUtil.d("validateEmail: $email")
    return when {
        email.isEmpty() -> "이메일을 입력하세요."
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "올바른 이메일 형식이 아닙니다."
        else -> null
    }
}