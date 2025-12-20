package com.example.tavern.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tavern.ui.theme.*

/**
 * REGISTRATION SCREEN - The Guestbook
 * Beautiful animated registration form
 */
@Composable
fun RegisterScreen(viewModel: TavernViewModel, onBackToLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var triggerShake by remember { mutableStateOf(false) }

    // Local error state for validation
    var validationError by remember { mutableStateOf<String?>(null) }
    
    // ViewModel error (e.g., User already exists)
    val vmError by viewModel.loginError.collectAsState()

    // Combined error
    val displayError = validationError ?: vmError

    // Trigger shake animation when error occurs
    LaunchedEffect(displayError) {
        if (displayError != null) {
            triggerShake = true
            kotlinx.coroutines.delay(500)
            triggerShake = false
        }
    }

    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Back Button with bounce animation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fadeInOnAppear(delayMillis = 50),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.bounceOnAppear()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Icon with pulse animation
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = Shapes.large,
                modifier = Modifier
                    .size(100.dp)
                    .pulseAnimation(minScale = 0.95f, maxScale = 1.05f, durationMillis = 2000)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = "Register",
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title with fade in animation
            Text(
                "Sign the Guestbook",
                style = TitleTavern.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fadeInOnAppear(delayMillis = 100)
            )

            Text(
                "Create your legend",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.fadeInOnAppear(delayMillis = 200)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username Field with slide animation
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    validationError = null // Clear error when typing
                },
                label = { Text("Choose a Name") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottomOnAppear(delayMillis = 300),
                shape = TextFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field with slide animation
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    validationError = null
                },
                label = { Text("Create Secret Word") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Filled.Visibility 
                            else 
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) 
                                "Hide password" 
                            else 
                                "Show password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottomOnAppear(delayMillis = 400),
                shape = TextFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field with slide animation
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    validationError = null
                },
                label = { Text("Repeat Secret Word") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                visualTransformation = if (confirmPasswordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) 
                                Icons.Filled.Visibility 
                            else 
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) 
                                "Hide password" 
                            else 
                                "Show password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottomOnAppear(delayMillis = 500)
                    .shakeAnimation(triggerShake),
                shape = TextFieldShape,
                isError = validationError != null || vmError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            // Password strength indicator
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthIndicator(
                    password = password,
                    modifier = Modifier.fadeInOnAppear(delayMillis = 100)
                )
            }

            // Error Display with animation
            AnimatedVisibility(
                visible = displayError != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = Shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = displayError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button with bounce animation
            Button(
                onClick = {
                    // Validation Logic
                    when {
                        username.isBlank() -> {
                            validationError = "Name cannot be empty!"
                        }
                        password.isBlank() -> {
                            validationError = "Password cannot be empty!"
                        }
                        password.length < 6 -> {
                            validationError = "Password must be at least 6 characters!"
                        }
                        password != confirmPassword -> {
                            validationError = "Passwords do not match!"
                        }
                        else -> {
                            validationError = null
                            viewModel.register(username, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .slideInFromBottomOnAppear(delayMillis = 600),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = ButtonShape,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.HistoryEdu,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Join the Guild",
                        style = ButtonText,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Already have account with fade animation
            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.fadeInOnAppear(delayMillis = 700)
            ) {
                Text(
                    "Already have a legend? Enter here",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Password Strength Indicator
 * Visual feedback for password strength
 */
@Composable
fun PasswordStrengthIndicator(password: String, modifier: Modifier = Modifier) {
    val strength = calculatePasswordStrength(password)
    val strengthText = when (strength) {
        0 -> "Very Weak"
        1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Strong"
        else -> "Very Strong"
    }
    val strengthColor = when (strength) {
        0 -> MaterialTheme.colorScheme.error
        1 -> Color(0xFFFF6F00) // Orange
        2 -> Color(0xFFFFC107) // Amber
        3 -> Color(0xFF8BC34A) // Light Green
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Strength:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                strengthText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = strengthColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (strength + 1) / 6f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = strengthColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/**
 * Calculate password strength (0-5)
 */
private fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    
    // Length check
    if (password.length >= 8) strength++
    if (password.length >= 12) strength++
    
    // Has uppercase
    if (password.any { it.isUpperCase() }) strength++
    
    // Has lowercase
    if (password.any { it.isLowerCase() }) strength++
    
    // Has digit
    if (password.any { it.isDigit() }) strength++
    
    // Has special character
    if (password.any { !it.isLetterOrDigit() }) strength++
    
    return strength.coerceAtMost(5)
}
