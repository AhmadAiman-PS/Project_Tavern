package com.example.tavern.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

/**
 * TAVERN ANIMATION SYSTEM
 * Smooth, organic animations for better UX
 */

// ===== ANIMATION SPECS =====

// Fast animations (for interactions)
val FastAnimation = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessHigh
)

// Medium animations (for most UI changes)
val MediumAnimation = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)

// Slow animations (for screens, large changes)
val SlowAnimation = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow
)

// Smooth tween animations
val SmoothTween = tween<Float>(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

// Quick tween animations
val QuickTween = tween<Float>(
    durationMillis = 150,
    easing = LinearOutSlowInEasing
)

// ===== FADE ANIMATIONS =====

val FadeIn = fadeIn(
    animationSpec = tween(300, easing = LinearOutSlowInEasing)
)

val FadeOut = fadeOut(
    animationSpec = tween(300, easing = FastOutLinearInEasing)
)

// ===== SLIDE ANIMATIONS =====

val SlideInFromRight = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + FadeIn

val SlideOutToLeft = slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + FadeOut

val SlideInFromBottom = slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + FadeIn

val SlideOutToBottom = slideOutVertically(
    targetOffsetY = { it },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + FadeOut

// ===== SCALE ANIMATIONS =====

val ScaleIn = scaleIn(
    initialScale = 0.8f,
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + FadeIn

val ScaleOut = scaleOut(
    targetScale = 0.8f,
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + FadeOut

// ===== CUSTOM COMPOSABLE ANIMATIONS =====

/**
 * Scale animation on press
 * Usage: Modifier.scaleOnPress()
 */
@Composable
fun Modifier.scaleOnPress(
    pressedScale: Float = 0.95f
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = FastAnimation,
        label = "scale"
    )
    
    return this.scale(scale)
}

/**
 * Shimmer loading animation
 * Usage: Modifier.shimmerEffect()
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    return this.graphicsLayer { this.alpha = alpha }
}

/**
 * Bounce animation on appearance
 * Usage: Modifier.bounceOnAppear()
 */
@Composable
fun Modifier.bounceOnAppear(): Modifier {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    return this.scale(scale.value)
}

/**
 * Fade in animation on appearance
 * Usage: Modifier.fadeInOnAppear()
 */
@Composable
fun Modifier.fadeInOnAppear(delayMillis: Int = 0): Modifier {
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    }
    
    return this.graphicsLayer { this.alpha = alpha.value }
}

/**
 * Slide in from bottom on appearance
 * Usage: Modifier.slideInFromBottomOnAppear()
 */
@Composable
fun Modifier.slideInFromBottomOnAppear(delayMillis: Int = 0): Modifier {
    val offsetY = remember { Animatable(300f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = LinearOutSlowInEasing)
            )
        }
    }
    
    return this.graphicsLayer {
        translationY = offsetY.value
        this.alpha = alpha.value
    }
}

/**
 * Rotate animation continuously
 * Usage: Modifier.rotateIndefinitely()
 */
@Composable
fun Modifier.rotateIndefinitely(durationMillis: Int = 2000): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    return this.graphicsLayer { rotationZ = rotation }
}

/**
 * Pulse animation (scale up and down)
 * Usage: Modifier.pulseAnimation()
 */
@Composable
fun Modifier.pulseAnimation(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 1000
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    return this.scale(scale)
}

/**
 * Shake animation (for errors)
 * Usage: triggerShake.value = true
 */
@Composable
fun Modifier.shakeAnimation(trigger: Boolean): Modifier {
    val offsetX = remember { Animatable(0f) }
    
    LaunchedEffect(trigger) {
        if (trigger) {
            for (i in 0..3) {
                offsetX.animateTo(
                    targetValue = if (i % 2 == 0) 10f else -10f,
                    animationSpec = tween(50)
                )
            }
            offsetX.animateTo(0f, animationSpec = tween(50))
        }
    }
    
    return this.graphicsLayer { translationX = offsetX.value }
}

// ===== SCREEN TRANSITION ANIMATIONS =====

/**
 * Enter transition for screens
 */
fun enterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

/**
 * Exit transition for screens
 */
fun exitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it / 2 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}

/**
 * Pop enter transition (back navigation)
 */
fun popEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -it / 2 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

/**
 * Pop exit transition (back navigation)
 */
fun popExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}
