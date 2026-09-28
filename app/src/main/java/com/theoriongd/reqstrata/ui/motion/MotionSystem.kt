package com.theoriongd.reqstrata.ui.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.ui.theme.AiAccent
import com.theoriongd.reqstrata.ui.theme.ErrorRed
import com.theoriongd.reqstrata.ui.theme.PrimaryViolet
import com.theoriongd.reqstrata.ui.theme.SuccessEmerald
import com.theoriongd.reqstrata.ui.theme.WarningAmber
import kotlinx.coroutines.delay

/**
 * CompositionLocal for accessibility "Reduce Motion" setting.
 * When enabled, animations avoid large movements, scaling, or infinite shimmering.
 */
val LocalReduceMotion = compositionLocalOf { false }
val LocalTactileFeedback = compositionLocalOf { true }

/**
 * Standardized Motion Durations for Reqstrata.
 * Designed to feel fast, connected, intentional, and responsive.
 */
object MotionDuration {
    /** Quick micro-interactions: icon switches, small fades, press feedback (120–160 ms) */
    const val FAST = 150

    /** Standard navigation transitions, tab switches, card size expansion (200–280 ms) */
    const val STANDARD = 240

    /** Emphasized transitions: Modal entries, dialogs, AI generation reveals (300–400 ms) */
    const val EMPHASIS = 340

    /** Shimmer animation cycle duration */
    const val SHIMMER = 1100

    /** Feedback display duration (e.g. copied checkmark) before resetting */
    const val FEEDBACK = 1600
}

/**
 * Standardized Motion Easing Curves conforming to Material 3 motion specs.
 */
object MotionEasing {
    /** Material 3 standard decelerate/emphasized curve */
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Emphasized Decelerate for smooth entrances */
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Emphasized Accelerate for quick exits */
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Natural deceleration */
    val Decelerate: Easing = FastOutSlowInEasing

    /** Constant rate */
    val Linear: Easing = LinearEasing
}

/**
 * Motion specifications generating consistent AnimationSpecs across UI components.
 */
object MotionSpec {
    fun <T> fastTween() = tween<T>(
        durationMillis = MotionDuration.FAST,
        easing = MotionEasing.Standard
    )

    fun <T> standardTween() = tween<T>(
        durationMillis = MotionDuration.STANDARD,
        easing = MotionEasing.Standard
    )

    fun <T> emphasisTween() = tween<T>(
        durationMillis = MotionDuration.EMPHASIS,
        easing = MotionEasing.EmphasizedDecelerate
    )

    fun <T> cardPressSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun contentSizeSpec(): FiniteAnimationSpec<IntSize> = tween(
        durationMillis = MotionDuration.STANDARD,
        easing = MotionEasing.Standard
    )
}

/**
 * Standard navigation transitions for NavHost and AnimatedContent.
 * All transitions respect the Reduce Motion accessibility setting.
 */
object MotionTransition {
    // Sibling Screen Navigation (Slide Horizontally + Fade)
    fun horizontalSlideEnter(isForward: Boolean = true, isReduceMotion: Boolean = false): EnterTransition {
        if (isReduceMotion) return fadeIn(animationSpec = MotionSpec.fastTween())
        val direction = if (isForward) 1 else -1
        return slideInHorizontally(
            initialOffsetX = { (it * 0.18f * direction).toInt() },
            animationSpec = MotionSpec.standardTween()
        ) + fadeIn(animationSpec = MotionSpec.standardTween())
    }

    fun horizontalSlideExit(isForward: Boolean = true, isReduceMotion: Boolean = false): ExitTransition {
        if (isReduceMotion) return fadeOut(animationSpec = MotionSpec.fastTween())
        val direction = if (isForward) -1 else 1
        return slideOutHorizontally(
            targetOffsetX = { (it * 0.18f * direction).toInt() },
            animationSpec = MotionSpec.standardTween()
        ) + fadeOut(animationSpec = MotionSpec.fastTween())
    }

    // Hierarchical Navigation (List -> Detail: Container Zoom & Slide)
    fun hierarchicalDetailEnter(isReduceMotion: Boolean = false): EnterTransition {
        if (isReduceMotion) return fadeIn(animationSpec = MotionSpec.standardTween())
        return slideInHorizontally(
            initialOffsetX = { (it * 0.22f).toInt() },
            animationSpec = MotionSpec.standardTween()
        ) + fadeIn(
            animationSpec = MotionSpec.standardTween()
        ) + scaleIn(
            initialScale = 0.97f,
            animationSpec = MotionSpec.standardTween()
        )
    }

    fun hierarchicalDetailExit(isReduceMotion: Boolean = false): ExitTransition {
        if (isReduceMotion) return fadeOut(animationSpec = MotionSpec.fastTween())
        return slideOutHorizontally(
            targetOffsetX = { (it * 0.22f).toInt() },
            animationSpec = MotionSpec.standardTween()
        ) + fadeOut(
            animationSpec = MotionSpec.fastTween()
        ) + scaleOut(
            targetScale = 0.97f,
            animationSpec = MotionSpec.fastTween()
        )
    }

    // Modal / Form Navigation (Dashboard -> Create: Slide Up + Fade)
    fun formModalEnter(isReduceMotion: Boolean = false): EnterTransition {
        if (isReduceMotion) return fadeIn(animationSpec = MotionSpec.standardTween())
        return slideInVertically(
            initialOffsetY = { (it * 0.14f).toInt() },
            animationSpec = MotionSpec.emphasisTween()
        ) + fadeIn(animationSpec = MotionSpec.standardTween())
    }

    fun formModalExit(isReduceMotion: Boolean = false): ExitTransition {
        if (isReduceMotion) return fadeOut(animationSpec = MotionSpec.fastTween())
        return slideOutVertically(
            targetOffsetY = { (it * 0.10f).toInt() },
            animationSpec = MotionSpec.standardTween()
        ) + fadeOut(animationSpec = MotionSpec.fastTween())
    }

    // Scale + Fade for important popups and result reveals
    fun scaleFadeEnter(isReduceMotion: Boolean = false): EnterTransition {
        if (isReduceMotion) return fadeIn(animationSpec = MotionSpec.standardTween())
        return fadeIn(animationSpec = MotionSpec.standardTween()) + scaleIn(
            initialScale = 0.96f,
            animationSpec = MotionSpec.standardTween()
        )
    }

    fun scaleFadeExit(isReduceMotion: Boolean = false): ExitTransition {
        if (isReduceMotion) return fadeOut(animationSpec = MotionSpec.fastTween())
        return fadeOut(animationSpec = MotionSpec.fastTween()) + scaleOut(
            targetScale = 0.96f,
            animationSpec = MotionSpec.fastTween()
        )
    }

    // Crossfade for Dashboard & Tab Transitions
    fun crossfadeEnter(): EnterTransition = fadeIn(animationSpec = MotionSpec.standardTween())
    fun crossfadeExit(): ExitTransition = fadeOut(animationSpec = MotionSpec.fastTween())
}

/**
 * Modifier for subtle card and item press interaction.
 * Scales down to ~0.98 on press without 3D tilt, magnetic drift, or elastic bouncing.
 * Bypasses scale when Reduce Motion is enabled.
 */
fun Modifier.pressScale(
    pressedScale: Float = 0.98f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    val tactileFeedback = LocalTactileFeedback.current
    if (reduceMotion || !tactileFeedback) {
        return@composed if (onClick != null) {
            Modifier.clickable { onClick() }
        } else Modifier
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = MotionSpec.cardPressSpring(),
        label = "card_press_scale"
    )

    this
        .scale(scale)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() }
            } else Modifier
        )
}

/**
 * Modifier for smooth animated height and size transitions on expandable containers.
 */
fun Modifier.expandableContentMotion(): Modifier = this.animateContentSize(
    animationSpec = MotionSpec.contentSizeSpec()
)

/**
 * Skeleton shimmer loading modifier with smooth infinite translation.
 * Bypasses shimmer and renders static baseColor when Reduce Motion is enabled.
 */
fun Modifier.skeletonShimmer(
    isShimmering: Boolean = true,
    baseColor: Color = Color(0xFF27272A),
    highlightColor: Color = Color(0xFF3F3F46)
): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    if (!isShimmering || reduceMotion) return@composed this.background(baseColor)

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -800f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MotionDuration.SHIMMER,
                easing = MotionEasing.Linear
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 400f, translateAnim + 400f)
    )

    this.background(brush)
}

/**
 * Localized subtle error shake effect.
 * Does NOT shake the whole screen; applies only to the target input or card.
 */
fun Modifier.subtleShake(
    trigger: Boolean,
    onComplete: (() -> Unit)? = null
): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    if (reduceMotion || !trigger) return@composed this

    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 300
                    0f at 0
                    -6f at 50
                    6f at 100
                    -4f at 150
                    4f at 200
                    -2f at 250
                    0f at 300
                }
            )
            onComplete?.invoke()
        }
    }

    this.offset { IntOffset(offsetX.value.toInt(), 0) }
}

/**
 * Subtle staggered entrance for items in lists or impact chains.
 */
fun Modifier.staggeredEntrance(
    index: Int,
    baseDelayMs: Int = 35
): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    if (reduceMotion) return@composed this

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val delayTime = (index.coerceAtMost(8) * baseDelayMs).toLong()
        delay(delayTime)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MotionSpec.standardTween(),
        label = "staggered_alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 10f,
        animationSpec = MotionSpec.standardTween(),
        label = "staggered_offset_y"
    )

    this
        .alpha(alpha)
        .offset { IntOffset(0, offsetY.toInt()) }
}

/**
 * Reusable Motion Card with subtle elevation and press feedback.
 */
@Composable
fun MotionCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(),
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(pressedScale = 0.985f, onClick = onClick),
        colors = colors,
        border = border,
        shape = shape,
        content = content
    )
}

/**
 * Reusable Motion Button with pressScale micro-interaction.
 */
@Composable
fun MotionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
    shape: Shape = RoundedCornerShape(10.dp),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.pressScale(pressedScale = 0.97f),
        enabled = enabled,
        colors = colors,
        shape = shape,
        content = content
    )
}

/**
 * Reusable Motion Copy Button that transforms smoothly between Copy Icon and Success Checkmark.
 */
@Composable
fun MotionCopyButton(
    textToCopy: String,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(MotionDuration.FEEDBACK.toLong())
            isCopied = false
        }
    }

    IconButton(
        onClick = {
            clipboardManager.setText(AnnotatedString(textToCopy))
            isCopied = true
        },
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = isCopied,
            transitionSpec = {
                (fadeIn(animationSpec = MotionSpec.fastTween()) + scaleIn(initialScale = 0.85f))
                    .togetherWith(fadeOut(animationSpec = MotionSpec.fastTween()) + scaleOut(targetScale = 0.85f))
            },
            label = "copy_button_morph"
        ) { copied ->
            if (copied) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Copied",
                        tint = SuccessEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    if (label != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copied", style = MaterialTheme.typography.labelSmall, color = SuccessEmerald)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy to clipboard",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    if (label != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * Animated Password Visibility Toggle with icon morph.
 */
@Composable
fun MotionPasswordToggle(
    isVisible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = isVisible,
            transitionSpec = {
                (fadeIn(animationSpec = MotionSpec.fastTween()) + scaleIn(initialScale = 0.85f))
                    .togetherWith(fadeOut(animationSpec = MotionSpec.fastTween()) + scaleOut(targetScale = 0.85f))
            },
            label = "password_visibility_morph"
        ) { visible ->
            Icon(
                imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (visible) "Hide password" else "Show password",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Animated Status Chip with subtle background and border transitions.
 */
@Composable
fun MotionStatusChip(
    statusText: String,
    isSelected: Boolean = false,
    color: Color = PrimaryViolet,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) color.copy(alpha = 0.25f) else color.copy(alpha = 0.12f),
        animationSpec = MotionSpec.fastTween(),
        label = "chip_bg_color"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) color else color.copy(alpha = 0.35f),
        animationSpec = MotionSpec.fastTween(),
        label = "chip_border_color"
    )

    Surface(
        color = animatedBg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, animatedBorder),
        modifier = modifier
            .pressScale(pressedScale = 0.97f) { onClick?.invoke() }
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Professional Expandable Section Card with rotating chevron and animated content size.
 */
@Composable
fun MotionExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    initiallyExpanded: Boolean = false,
    badgeText: String? = null,
    badgeColor: Color = PrimaryViolet,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = MotionSpec.standardTween(),
        label = "chevron_rotation"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .pressScale(pressedScale = 0.99f) { expanded = !expanded }
            .expandableContentMotion()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PrimaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(chevronRotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = MotionSpec.standardTween()) + expandVertically(animationSpec = MotionSpec.standardTween()),
                exit = fadeOut(animationSpec = MotionSpec.fastTween()) + shrinkVertically(animationSpec = MotionSpec.fastTween())
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

/**
 * Reusable AI Generation Motion Container.
 * Manages the state machine:
 * IDLE -> GENERATING -> PROCESSING -> STRUCTURED RESULT REVEAL -> REVIEW -> ACCEPT/REJECT.
 */
@Composable
fun AiGenerationMotionContainer(
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    generationTitle: String = "AI Synthesis",
    generationSubtitle: String = "Analyzing system specifications...",
    hasResult: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    resultContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .expandableContentMotion()
    ) {
        // Generating / Processing Banner with established Violet #7C3AED identity
        AnimatedVisibility(
            visible = isGenerating,
            enter = fadeIn(animationSpec = MotionSpec.standardTween()) + expandVertically(animationSpec = MotionSpec.standardTween()),
            exit = fadeOut(animationSpec = MotionSpec.fastTween()) + shrinkVertically(animationSpec = MotionSpec.fastTween())
        ) {
            Surface(
                color = Color(0xFF2E1065).copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF7C3AED).copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFA78BFA),
                            strokeWidth = 2.dp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = generationTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA78BFA)
                        )
                        Text(
                            text = generationSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Error Feedback Banner
        AnimatedVisibility(
            visible = errorMessage != null && !isGenerating,
            enter = fadeIn(animationSpec = MotionSpec.fastTween()) + expandVertically(animationSpec = MotionSpec.fastTween()),
            exit = fadeOut(animationSpec = MotionSpec.fastTween()) + shrinkVertically(animationSpec = MotionSpec.fastTween())
        ) {
            errorMessage?.let { error ->
                Surface(
                    color = Color(0xFF450A0A).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5),
                            modifier = Modifier.weight(1f)
                        )
                        if (onRetry != null) {
                            TextButton(onClick = onRetry) {
                                Text("Retry", color = ErrorRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Result Container Entrance
        AnimatedVisibility(
            visible = hasResult && !isGenerating,
            enter = fadeIn(animationSpec = MotionSpec.emphasisTween()) + scaleIn(
                initialScale = 0.98f,
                animationSpec = MotionSpec.emphasisTween()
            ),
            exit = fadeOut(animationSpec = MotionSpec.fastTween())
        ) {
            resultContent()
        }
    }
}

/**
 * Test Execution State Badge with animated state transitions.
 * Communicates: Idle -> Running (Spinner) -> Passed / Failed / Blocked.
 */
@Composable
fun TestStatusMotionBadge(
    status: String,
    isExecuting: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when {
        isExecuting -> Pair(PrimaryViolet, null)
        status.equals("PASSED", ignoreCase = true) -> Pair(SuccessEmerald, Icons.Default.CheckCircle)
        status.equals("FAILED", ignoreCase = true) -> Pair(ErrorRed, Icons.Default.Cancel)
        status.equals("BLOCKED", ignoreCase = true) -> Pair(WarningAmber, Icons.Default.Block)
        else -> Pair(Color(0xFFA1A1AA), Icons.Default.RadioButtonUnchecked)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isExecuting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = color,
                    strokeWidth = 1.5.dp
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = status,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isExecuting) "Running..." else status,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Skeleton Loader Component matching the structured layout of engineering cards.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    lines: Int = 3
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .skeletonShimmer()
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .skeletonShimmer()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            repeat(lines) { index ->
                val widthFraction = when (index) {
                    0 -> 0.95f
                    1 -> 0.8f
                    else -> 0.6f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(widthFraction)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .skeletonShimmer()
                )
                if (index < lines - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

/**
 * Professional Animated Tab Row for Reqstrata.
 * Provides a unified, smooth animated indicator and typography across all tabbed screens.
 */
@Composable
fun ReqstrataTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = PrimaryViolet,
        edgePadding = 12.dp,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = PrimaryViolet
                )
            }
        },
        divider = {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PrimaryViolet else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}
