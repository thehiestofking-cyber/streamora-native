package site.everyniche.streamora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import site.everyniche.streamora.data.network.SupabaseAuth
import site.everyniche.streamora.ui.components.clickableNoIndication
import site.everyniche.streamora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(auth: SupabaseAuth, onComplete: () -> Unit) {
    var isSignup by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        error = null; notice = null
        if (email.isBlank() || password.isBlank()) { error = "Enter your email and password."; return }
        if (isSignup && password.length < 6) { error = "Password must be at least 6 characters."; return }
        busy = true
        scope.launch {
            val result = if (isSignup) auth.signUp(email.trim(), password, name.ifBlank { email.substringBefore("@") })
            else auth.signIn(email.trim(), password)
            busy = false
            result.onSuccess {
                if (isSignup) {
                    notice = "Account created! Check your email to confirm, then sign in."
                    isSignup = false
                } else {
                    onComplete()
                }
            }.onFailure {
                error = it.message ?: "Something went wrong."
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Brush.linearGradient(listOf(AccentBlue, AccentBlueDark))),
                    contentAlignment = Alignment.Center,
                ) { Text("▶", color = Color.White, fontSize = 13.sp) }
                Spacer(Modifier.width(8.dp))
                Text("STREAM", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("ORA", color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(36.dp))
            Text(
                if (isSignup) "Create account" else "Welcome back",
                color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
            )
            Text(
                if (isSignup) "Join Streamora for premium streaming" else "Sign in to continue watching",
                color = TextMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp),
            )

            Spacer(Modifier.height(28.dp))

            if (error != null) {
                MessageBanner(error!!, isError = true)
                Spacer(Modifier.height(12.dp))
            }
            if (notice != null) {
                MessageBanner(notice!!, isError = false)
                Spacer(Modifier.height(12.dp))
            }

            if (isSignup) {
                AuthTextField(value = name, onValueChange = { name = it }, placeholder = "Full name")
                Spacer(Modifier.height(12.dp))
            }
            AuthTextField(value = email, onValueChange = { email = it }, placeholder = "Email address", keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = password, onValueChange = { password = it }, placeholder = "Password",
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailing = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) "Hide" else "Show", color = TextMuted, fontSize = 12.sp)
                    }
                },
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { submit() },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            ) {
                Text(
                    if (busy) "Please wait…" else if (isSignup) "Create Account" else "Sign In",
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    if (isSignup) "Already have an account? " else "Don't have an account? ",
                    color = TextMuted, fontSize = 14.sp,
                )
                Text(
                    if (isSignup) "Sign in" else "Sign up",
                    color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickableNoIndication { isSignup = !isSignup; error = null; notice = null },
                )
            }
        }
    }
}

@Composable
private fun MessageBanner(text: String, isError: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isError) Color(0x1FEF4444) else Color(0x1F10B981))
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Text(text, color = if (isError) Color(0xFFFCA5A5) else Color(0xFF6EE7B7), fontSize = 13.sp)
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted) },
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = trailing,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0x0DFFFFFF),
            unfocusedContainerColor = Color(0x0DFFFFFF),
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = Color(0x1AFFFFFF),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
    )
}
