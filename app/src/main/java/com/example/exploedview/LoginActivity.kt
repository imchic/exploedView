package com.example.exploedview

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exploedview.components.NeoButton
import com.example.exploedview.components.NeoTextField

/*
class LoginActivity: BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override val layoutId: Int = R.layout.activity_login
    override val vm: LoginViewModel = LoginViewModel()

    private val context = this@LoginActivity

    override fun initViewStart() {}

    override fun initDataBinding() {
        CoroutineScope(Dispatchers.Main).launch {
            repeatOnStarted {
                vm.loginEventFlow.collect { event ->
                    handleLoginEvent(event)
                }
            }
        }
    }

    override fun initViewFinal() {
        binding.btnLogin.setOnClickListener {
//            val email = binding.etEmail.text.toString()
//            val password = binding.etPassword.text.toString()
//            vm.loginEvent(LoginViewModel.LoginEvent.Login(email, password))
        }
    }

    private fun handleLoginEvent(event: LoginViewModel.LoginEvent) {
        when (event) {
            is LoginViewModel.LoginEvent.Login -> {
                // 로그인 요청
                // 로그인 성공 시 LoginSuccess 이벤트 발생
                // 로그인 실패 시 LoginFail 이벤트 발생

                LogUtil.d("id: ${event.id}, pw: ${event.pw}")

                // for example
                if (event.id == "test" && event.pw == "1234") {
                    vm.loginEvent(LoginViewModel.LoginEvent.LoginSuccess("token"))
                } else {
                    vm.loginEvent(LoginViewModel.LoginEvent.LoginFail("login fail"))
                }

            }
            is LoginViewModel.LoginEvent.LoginSuccess -> {
                // 로그인 성공 시 처리
                Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
            }
            is LoginViewModel.LoginEvent.LoginFail -> {
                // 로그인 실패 시 처리
                Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

}*/


// 컴포즈 버전
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {

                var isLoginSuccess by remember { mutableStateOf(false) }

                // 테마지정


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
    fun LoginScreen(onLoginSuccess: () -> Unit) {

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(true) }

        var emailError by remember { mutableStateOf<String?>(null) }
        var passwordError by remember { mutableStateOf<String?>(null) }
        var loginError by remember { mutableStateOf<String?>(null) }

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
                            .weight(0.45f) // 가로 1:1 비율 유지
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
                                    width = 108.dp,
                                    height = 48.dp
                                )
                        )

                        // 이미지 간격없이 진행
                        Image(
                            painter = painterResource(id = R.drawable.smartblueprint),
                            contentDescription = null,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(208.dp)
                        )

                        // 정렬 방향이 세로이므로 Row를 사용하여 가로 정렬
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.Center,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.img_neobrix_logo),
//                                contentDescription = null,
//                                alignment = Alignment.Center,
//                                modifier = Modifier
//                                    .size(108.dp)
//                                    .padding(8.dp)
//                                    .padding(top = 6.dp),
//                            )
////                        Text("공동주택전개도", fontSize = 28.sp, fontWeight = FontWeight.Bold)
//                            NeoText("공동주택전개도", fontSize = 24.sp, fontWeight = FontWeight.Bold)
//                        }

                        Spacer(modifier = Modifier.height(16.dp))

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
                        )

                        Spacer(modifier = Modifier.height(16.dp))

//                    Button(
//                        onClick = {
//                            if (email.isNotEmpty() && password.isNotEmpty()) {
//                                // 로그인 성공 (여기서는 단순 체크)
//                                onLoginSuccess()
//                            } else {
//                                emailError = "이메일과 비밀번호를 입력하세요"
//                            }
////                            onLoginSuccess()
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(48.dp)
//                            .background(
//                                brush = Brush.linearGradient(
//                                    colors = listOf(
//                                        Color(0xff5038ED), Color(0xff9181F4)
//                                    )
//                                ),
//                                shape = RoundedCornerShape(16.dp)
//                            ),
//
//                        colors = ButtonDefaults.buttonColors(
//                            contentColor = Color.White,
//                            containerColor = Color.Transparent,
//                            disabledContentColor = Color.Gray,
//                            disabledContainerColor = Color.Transparent
//                        ),
//                        enabled = email.isNotEmpty() && password.isNotEmpty()
//                    ) {
//                        Text("로그인")
//                    }

//                    NeoButton

                        NeoButton(
                            text = "로그인",
                            modifer = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xff5038ED), Color(0xff9181F4)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
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

//                    TextButton(onClick = { /* 회원가입 화면 이동 */ }) {
//                        Text("회원가입")
//                    }
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
                            .padding(16.dp)
                            .weight(0.5f) // 가로 1:1 비율 유지
                            .padding(end = 8.dp), // 우측과의 간격
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        // 우측에 로고
                        Image(
                            painter = painterResource(id = R.drawable.img_neobrix_logo),
                            contentDescription = null,
                            alignment = Alignment.TopEnd,
                            modifier = Modifier
                                .size(
                                    width = 108.dp,
                                    height = 48.dp
                                )
                        )

                        Image(
                            painter = painterResource(id = R.drawable.smartblueprint),
                            contentDescription = null,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(208.dp)
                        )

                        // 정렬 방향이 세로이므로 Row를 사용하여 가로 정렬
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.img_neobrix_logo),
//                                contentDescription = null,
//                                alignment = Alignment.Center,
//                                modifier = Modifier
//                                    .size(108.dp)
//                                    .padding(8.dp)
//                            )
//                        Text("공동주택전개도", fontSize = 28.sp, fontWeight = FontWeight.Bold)
//                            NeoText("공동주택전개도", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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
                        )

                        Spacer(modifier = Modifier.height(16.dp))

//                    Button(
//                        onClick = {
//                            if (email.isNotEmpty() && password.isNotEmpty()) {
//                                // 로그인 성공 (여기서는 단순 체크)
//                                onLoginSuccess()
//                            } else {
//                                emailError = "이메일과 비밀번호를 입력하세요"
//                            }
////                            onLoginSuccess()
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(48.dp)
//                            .background(
//                                brush = Brush.linearGradient(
//                                    colors = listOf(
//                                        Color(0xff5038ED), Color(0xff9181F4)
//                                    )
//                                ),
//                                shape = RoundedCornerShape(16.dp)
//                            ),
//
//                        colors = ButtonDefaults.buttonColors(
//                            contentColor = Color.White,
//                            containerColor = Color.Transparent,
//                            disabledContentColor = Color.Gray,
//                            disabledContainerColor = Color.Transparent
//                        ),
//                        enabled = email.isNotEmpty() && password.isNotEmpty()
//                    ) {
//                        Text("로그인")
//                    }

//                    NeoButton

                        NeoButton(
                            text = "로그인",
                            modifer = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xff5038ED), Color(0xff9181F4)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
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

//                    TextButton(onClick = { /* 회원가입 화면 이동 */ }) {
//                        Text("회원가입")
//                    }
                    }

                }

            }
        }

    }

    @Composable
    fun MainScreen(onLogout: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("로그인 성공", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogout) {
                Text("로그아웃")
            }
        }
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "이메일을 입력하세요."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "올바른 이메일 형식이 아닙니다."
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "비밀번호를 입력하세요."
            password.length < 6 -> "비밀번호는 최소 6자 이상이어야 합니다."
            !password.any { it.isDigit() } -> "비밀번호에 숫자를 포함해야 합니다."
            else -> null
        }
    }

}

@Composable
fun CustomSnackbar(
    message: String,
    actionText: String,
    action: () -> Unit,
) {

    var isSnackbarVisible by remember { mutableStateOf(true) }

    if (isSnackbarVisible) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xCC000000),
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(message)
                TextButton(onClick = {
                    isSnackbarVisible = false
                    action()
                }) {
                    Text(actionText)
                }
            }
        }
    }

}