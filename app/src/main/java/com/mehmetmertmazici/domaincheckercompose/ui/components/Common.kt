package com.mehmetmertmazici.domaincheckercompose.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}

/**
 * Animated list item wrapper for smooth fade-in and slide-up animations
 */
@Composable
fun AnimatedListItem(
    visible: Boolean = true,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    // We use MutableTransitionState to force the animation to run even if 'visible' is true initially.
    // We start with 'false' (invisible) and update to 'visible' argument.
    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = visible
        }
    }

    // React to external visibility changes
    LaunchedEffect(visible) {
        visibleState.targetState = visible
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 600, delayMillis = delayMillis)
        ) + slideInVertically(
            animationSpec = tween(durationMillis = 600, delayMillis = delayMillis),
            initialOffsetY = { it / 2 }
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        content()
    }
}

/**
 * Premium Card with support for glowing borders, blur effects and click animations
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isHot: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val scaleModifier = if (onClick != null) Modifier.scaleClick(onClick) else Modifier
    
    // Border for hot items
    val borderStroke = if (isHot) {
        BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary
                )
            )
        )
    } else {
        BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }

    // Shadow evaluation based on hot status
    val shadowElevation = if (isHot) 8.dp else 2.dp
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(scaleModifier),
        shape = RoundedCornerShape(24.dp), // Extra large corners
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Slight transparency
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Standardized icon container with soft background
 */
@Composable
fun IconSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = backgroundColor.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Icon content with specific tint usually passed from outside, 
        // but we assume content here handles the icon rendering.
        // If we want to enforce tint, we could wrapping CompositionLocalProvider.
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            content()
        }
    }
}

/**
 * Click modifier that adds a scale/bounce effect
 */
@Composable
fun Modifier.scaleClick(
    onClick: () -> Unit,
    scaleDown: Float = 0.95f
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull()
                    
                    if (change != null) {
                        isPressed = change.pressed
                    } else {
                        isPressed = false
                    }
                }
            }
        }
}