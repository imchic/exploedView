package com.example.exploedview

import ForgotPasswordScreen
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exploedview.components.NeoButton
import com.example.exploedview.components.NeoTextField
import com.example.exploedview.util.LogUtil

// 컴포즈 버전
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {

                var isLoginSuccess by remember { mutableStateOf(false) }

                if (isLoginSuccess) {

                    // MapActivity로 이동
                    val intent = Intent(this@LoginActivity, NaverMapActivity::class.java)
                    startActivity(intent)


                } else {
                    LoginScreen {
                        isLoginSuccess = true
                    }
                }

            }
        }
    }

    @Composable
    fun TabletLoginScreen() {
        // 이메일, 비밀번호 상태 저장
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        // 로그인 버튼 클릭 시 동작
        val onLoginClick = {
            // 로그인 처리 로직
            println("로그인 시도: 이메일 = $email, 비밀번호 = $password")
        }

        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween, // 이미지와 폼을 좌우로 배치
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측: 브랜드 또는 이미지
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(), Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_innobrick2), // 대체할 이미지 추가
                    contentDescription = "Brand Image", modifier = Modifier.fillMaxWidth()
                )
            }

            // 우측: 로그인 폼
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)) // 부드러운 회색 배경
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // 이메일 입력 필드
                    NeoTextField(
                        value = email,
                        onValueChange = { email = it },
                        labelText = "이메일",
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 비밀번호 입력 필드
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("비밀번호") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                    )

                    // 로그인 버튼
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(8.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = Color(0xFF1A73E8), // 버튼 배경색 파란색
//                            contentColor = Color.White // 버튼 텍스트 색상 흰색
//                        )
                    ) {
                        Text("로그인")
                    }

                    // 비밀번호 찾기 링크
                    TextButton(onClick = { /* 비밀번호 찾기 로직 */ }) {
                        Text("비밀번호를 잊으셨나요?", color = Color.Gray)
                    }
                }
            }
        }
    }

    @Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
    @Composable
    fun PreviewTabletLoginScreen() {
        TabletLoginScreen()
    }


    @Composable
    fun LoginScreen(onLoginSuccess: () -> Unit) {

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(true) }
        var isFindPassword by remember { mutableStateOf(false) }

        var emailError by remember { mutableStateOf<String?>(null) }
        var passwordError by remember { mutableStateOf<String?>(null) }
        var loginError by remember { mutableStateOf<String?>(null) }

        if (isFindPassword) {
            ForgotPasswordScreen(
                onDismiss = { isFindPassword = false },
                onConfirm = { isFindPassword = false },
                isFindPassword,
            )
        }

        // 현재 화면의 방향을 가져옴 (가로 또는 세로)
        val orientation = LocalConfiguration.current.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .weight(1f) // 가로 1:1 비율을 유지하는 Row
                ) {
                    // 좌측 영역 (예: 이메일 입력 필드)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.25f) // 가로 1:1 비율 유지
                            .padding(end = 8.dp) // 우측과의 간격
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_login_bg),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }

                    // 우측 영역 (예: 비밀번호 입력 필드)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .weight(0.5f), // 가로 1:1 비율 유지
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.img_neobrix_logo),
                            contentDescription = null,
                            alignment = Alignment.TopEnd,
                            modifier = Modifier
                                //.fillMaxWidth()
                                .size(
                                    width = 108.dp, height = 48.dp
                                )
                        )

                        // 이미지 간격없이 진행
                        Image(
                            painter = painterResource(id = R.drawable.ic_innobrick2),
                            contentDescription = null,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        NeoTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = validateEmail(it)
                            },
                            labelText = "이메일",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            trailingIcon = {
                                // 이메일 삭제 버튼
                                IconButton(onClick = {
                                    email = ""
                                    emailError = null
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear, contentDescription = null
                                    )
                                }
                            },
                            isError = emailError != null,
                            isErrorText = emailError ?: "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)

                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        NeoTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = validatePassword(it)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                // 비밀번호 보이기/숨기기 버튼
                                IconButton(onClick = {
                                    isPasswordVisible = !isPasswordVisible
                                }) {
                                    Icon(
                                        //imageVector = if (isPasswordVisible) Icons.Default.Clear else Icons.Outlined.Lock,
                                        painter = if (isPasswordVisible) painterResource(id = R.drawable.ic_visibility_off) else painterResource(
                                            id = R.drawable.ic_visibility_on
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            // 비밀번호일 경우에만 숨기기
                            visualTransformation = if (isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                            labelText = "비밀번호",
                            isError = passwordError != null,
                            isErrorText = passwordError ?: "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                            // 비밀번호 찾기 링크
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        NeoButton(
                            text = "로그인",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xff5038ED), Color(0xff9181F4)
                                        )
                                    ), shape = RoundedCornerShape(16.dp)
                                ),
                            onClick = {
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    // 로그인 성공 (여기서는 단순 체크)
                                    onLoginSuccess()
                                } else {
                                    emailError = "이메일과 비밀번호를 입력하세요"
                                }
                            },
                            enabled = email.isNotEmpty() && password.isNotEmpty()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                    }

                }

            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .weight(1f) // 가로 1:1 비율을 유지하는 Row
                ) {
                    // 좌측 영역 (예: 이메일 입력 필드)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f) // 가로 1:1 비율 유지
                            .padding(end = 8.dp) // 우측과의 간격
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_login_bg),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 우측 영역 (예: 비밀번호 입력 필드)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .weight(0.75f) // 가로 1:1 비율 유지
                            .padding(end = 8.dp), // 우측과의 간격
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        // 우측 상단에 로고
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_neobrix_logo),
                                contentDescription = null,
                                alignment = Alignment.TopEnd,
                                modifier = Modifier
                                    .size(
                                        width = 88.dp, height = 24.dp
                                    )
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.ic_innobrick2),
                            contentDescription = null,
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(
                                    width = 300.dp, height = 150.dp
                                )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "공동주택전개도 시스템",
                                fontSize = 30.sp,
                                color = Color(0xFF616161),
                                style = TextStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.15.sp,
                                    color = Color(0xFF616161),
                                ),
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                modifier = Modifier
                                    .background(
                                        Color.Blue,
                                        shape = RoundedCornerShape(8.dp)
                                    ) // 배경 색과 둥글게 처리
                                    .padding(horizontal = 8.dp), // 수평 방향으로만 여백 설정여백
                                color = Color.Transparent // 배경만 색상 지정
                            ) {
                                // 버전
                                Text(
                                    text = "v1.0.0",
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    style = TextStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.15.sp,
                                        color = Color(0xFF333333),
                                    ),
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
//                                .shadow(
//                                    elevation = 14.dp,
//                                    spotColor = Color(0x05000000),
//                                    ambientColor = Color(0x05000000)
//                                )
//                                .shadow(
//                                    elevation = 25.dp,
//                                    spotColor = Color(0x05000000),
//                                    ambientColor = Color(0x05000000)
//                                )
//                                .shadow(
//                                    elevation = 34.dp,
//                                    spotColor = Color(0x03000000),
//                                    ambientColor = Color(0x03000000)
//                                )
//                                .shadow(
//                                    elevation = 40.dp,
//                                    spotColor = Color(0x00000000),
//                                    ambientColor = Color(0x00000000)
//                                )
//                                .shadow(
//                                    elevation = 44.dp,
//                                    spotColor = Color(0x00000000),
//                                    ambientColor = Color(0x00000000)
//                                ),
                            shape = RoundedCornerShape(16.dp),
                            // 거의 흰색에 가깝게
                            colors = CardDefaults.cardColors(containerColor = Color(0xffffff)), // 부드러운 회색 배경
                            //border = BorderStroke(1.dp, Color(0xFFEAEDEE))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 이메일 입력 필드
                                NeoTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it
                                        emailError = validateEmail(it)
                                    },
                                    labelText = "이메일",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    trailingIcon = {
                                        // 이메일 삭제 버튼
                                        IconButton(onClick = {
                                            email = ""
                                            emailError = null
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    isError = emailError != null,
                                    isErrorText = emailError ?: "",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                NeoTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordError = validatePassword(it)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        // 비밀번호 보이기/숨기기 버튼
                                        IconButton(onClick = {
                                            isPasswordVisible = !isPasswordVisible
                                        }) {
                                            Icon(
                                                //imageVector = if (isPasswordVisible) Icons.Default.Clear else Icons.Outlined.Lock,
                                                painter = if (isPasswordVisible) painterResource(id = R.drawable.ic_visibility_off) else painterResource(
                                                    id = R.drawable.ic_visibility_on
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    // 비밀번호일 경우에만 숨기기
                                    visualTransformation = if (isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                                    labelText = "비밀번호",
                                    isError = passwordError != null,
                                    isErrorText = passwordError ?: "",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )

                                // 로그인 버튼
                                NeoButton(
                                    text = "로그인",
                                    onClick = {
                                        if (email.isNotEmpty() && password.isNotEmpty()) {
                                            // 로그인 성공 (여기서는 단순 체크)
                                            onLoginSuccess()
                                        } else {
                                            emailError = "이메일과 비밀번호를 입력하세요"
                                        }
                                    },
                                    enabled = email.isNotEmpty() && password.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 비밀번호 찾기 링크

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_lock_24),
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    // 비밀번호 찾기 링크
                                    TextButton(onClick = {
                                        isFindPassword = true
                                    }) {
                                        Text("비밀번호를 잊으셨나요?", color = Color.Gray)
                                    }
                                }
                            }
                        }

                    }

                }

            }
        }

    }

    private fun validateEmail(email: String): String? {
        LogUtil.d("validateEmail: $email")
        return when {
            email.isEmpty() -> "이메일을 입력하세요."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "올바른 이메일 형식이 아닙니다."
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "비밀번호를 입력하세요."
            password.length < 6 -> "비밀번호는 최소 6자 이상이어야 합니다."
            !password.any { it.isDigit() } -> "비밀번호에 숫자를 포함해야 합니다."
            else -> null
        }
    }

}

