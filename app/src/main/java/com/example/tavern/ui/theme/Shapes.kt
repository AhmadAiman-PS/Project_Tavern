package com.example.tavern.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * TAVERN SHAPE SYSTEM
 * Rounded corners for a softer, more organic medieval feel
 */

val Shapes = Shapes(
    // Extra Small - Chips, small buttons
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - Small cards, buttons
    small = RoundedCornerShape(8.dp),
    
    // Medium - Standard cards, dialogs
    medium = RoundedCornerShape(12.dp),
    
    // Large - Large cards, bottom sheets
    large = RoundedCornerShape(16.dp),
    
    // Extra Large - Full screen dialogs
    extraLarge = RoundedCornerShape(24.dp)
)

// ===== CUSTOM SHAPES FOR SPECIFIC COMPONENTS =====

// Post Cards - Asymmetric for handwritten paper feel
val PostCardShape = RoundedCornerShape(
    topStart = 12.dp,
    topEnd = 16.dp,
    bottomEnd = 12.dp,
    bottomStart = 16.dp
)

// Comment Cards - Smaller, tighter corners
val CommentCardShape = RoundedCornerShape(
    topStart = 8.dp,
    topEnd = 12.dp,
    bottomEnd = 8.dp,
    bottomStart = 12.dp
)

// Dialog Shape - More rounded for prominence
val DialogShape = RoundedCornerShape(20.dp)

// Button Shape - Classic rounded
val ButtonShape = RoundedCornerShape(8.dp)

// TextField Shape - Subtle rounding
val TextFieldShape = RoundedCornerShape(8.dp)

// FAB Shape - Circular feel
val FabShape = RoundedCornerShape(16.dp)

// Top Bar Shape - Flat top, rounded bottom
val TopBarShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp
)

// Bottom Bar Shape - Rounded top, flat bottom
val BottomBarShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)
