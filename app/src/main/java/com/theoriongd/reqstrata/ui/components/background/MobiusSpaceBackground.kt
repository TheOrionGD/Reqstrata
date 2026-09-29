package com.theoriongd.reqstrata.ui.components.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.theoriongd.reqstrata.ui.components.mobius.drawCyberpunkMobiusRibbon
import com.theoriongd.reqstrata.ui.components.mobius.rememberCyberpunkMobiusState
import com.theoriongd.reqstrata.ui.motion.LocalReduceMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// 3D Immersive Cosmic Mobius Space Background
// Deep cosmic void, atmospheric nebulae, 3D starfield, meteors,
// and the production 3D Cyberpunk Möbius ribbon engine.
// ─────────────────────────────────────────────────────────────────────────────

internal class StarParticle(
    val xNorm: Float,       // [-1, 1]
    val yNorm: Float,       // [-1, 1]
    val zNorm: Float,       // [-1, 1]
    val vxNorm: Float,      // Continuous 3D drift
    val vyNorm: Float,
    val vzNorm: Float,
    val size: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Float,
    val twinklePhase: Float,
    val starType: Int       // 0=white, 1=cyan, 2=violet, 3=gold
)

internal class ShootingStar(
    var x: Float,
    var y: Float,
    var length: Float,
    var speed: Float,
    var angleRad: Float,
    var alpha: Float,
    var active: Boolean
)

private fun generateStars(count: Int): Array<StarParticle> {
    val rng = kotlin.random.Random(0x3B89C2F1.toInt())
    return Array(count) {
        val roll = rng.nextFloat()
        StarParticle(
            xNorm = rng.nextFloat() * 2f - 1f,
            yNorm = rng.nextFloat() * 2f - 1f,
            zNorm = rng.nextFloat() * 2f - 1f,
            vxNorm = (rng.nextFloat() - 0.5f) * 0.025f,
            vyNorm = (rng.nextFloat() - 0.5f) * 0.020f,
            vzNorm = (rng.nextFloat() - 0.5f) * 0.030f,
            size = rng.nextFloat() * 2.8f + 0.6f,
            baseAlpha = rng.nextFloat() * 0.65f + 0.15f,
            twinkleSpeed = rng.nextFloat() * 2.2f + 0.8f,
            twinklePhase = rng.nextFloat() * 2f * PI.toFloat(),
            starType = when {
                roll < 0.65f -> 0  // Pure celestial white/silver
                roll < 0.82f -> 1  // Cosmic cyan / electric blue
                roll < 0.94f -> 2  // Neon violet / magenta
                else         -> 3  // Astral gold
            }
        )
    }
}

/**
 * High-performance 3D Immersive Cosmic Mobius Space Canvas.
 * Renders an infinite, undulating, 3D twisted Mobius ribbon that traverses space
 * with volumetric nebulae, floating starfields, shooting stars, and live energy pulses.
 */
@Composable
fun MobiusSpaceBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 220,
    rotationSpeed: Float = 1f,
    intensity: Float = 1f,
    enabled: Boolean = true,
    showRibbon: Boolean = true,
    showOrbitalRings: Boolean = true,
    showCenterCore: Boolean = false,
    ribbonScale: Float = 1f,
    ribbonOffsetY: Float = 0f
) {
    val reduceMotion = LocalReduceMotion.current
    val clampedStars = particleCount.coerceIn(60, 450)
    val stars = remember(clampedStars) { generateStars(clampedStars) }

    // Pre-allocated scratch buffers for 3D Cyberpunk ribbon
    val mobiusState = rememberCyberpunkMobiusState()

    // Dynamic shooting stars pool
    val shootingStars = remember {
        Array(3) {
            ShootingStar(
                x = 0f,
                y = 0f,
                length = 120f,
                speed = 450f,
                angleRad = 0.785f,
                alpha = 0f,
                active = false
            )
        }
    }

    var timeNanos by remember { mutableLongStateOf(0L) }
    var startNanos by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(enabled, reduceMotion) {
        if (enabled && !reduceMotion) {
            while (true) {
                withFrameNanos { frameNanos ->
                    if (startNanos < 0L) startNanos = frameNanos
                    timeNanos = frameNanos - startNanos
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val timeSec = if (reduceMotion || !enabled) 0.5f else (timeNanos / 1_000_000_000f) * rotationSpeed

        val w = size.width
        val h = size.height
        val minDim = size.minDimension
        val cx = w * 0.5f
        val cy = h * 0.5f
        val ws = (minDim / 900f).coerceIn(0.4f, 2.2f)

        // 1. Deep Space Cosmic Background Void
        drawRect(color = Color(0xFF08070E))

        // 2. Multi-Nebula Atmospheric Volumetric Glows (dynamic subtle shifting)
        val neb1X = cx + sin(timeSec * 0.18f) * (w * 0.22f)
        val neb1Y = cy + cos(timeSec * 0.14f) * (h * 0.18f)
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF7C3AED).copy(alpha = 0.08f * intensity),
                    0.5f to Color(0xFF4C1D95).copy(alpha = 0.035f * intensity),
                    1.0f to Color.Transparent
                ),
                center = Offset(neb1X, neb1Y),
                radius = minDim * 0.65f
            ),
            radius = minDim * 0.65f,
            center = Offset(neb1X, neb1Y)
        )

        val neb2X = cx + cos(timeSec * 0.12f) * (w * 0.25f)
        val neb2Y = cy + sin(timeSec * 0.16f) * (h * 0.22f)
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF06B6D4).copy(alpha = 0.045f * intensity),
                    0.6f to Color(0xFF1E1B4B).copy(alpha = 0.02f * intensity),
                    1.0f to Color.Transparent
                ),
                center = Offset(neb2X, neb2Y),
                radius = minDim * 0.55f
            ),
            radius = minDim * 0.55f,
            center = Offset(neb2X, neb2Y)
        )

        // 3. Multi-Layer 3D Floating Starfield (draw behind Mobius)
        val camZ = 750f * ws
        val fov = 850f * ws
        drawDeepSpaceStarfield(
            stars = stars,
            timeSec = timeSec,
            cx = cx,
            cy = cy,
            w = w,
            h = h,
            camZ = camZ,
            fov = fov,
            ws = ws,
            intensity = intensity
        )

        // 4. Shooting Stars (subtle energetic streaks)
        if (!reduceMotion && enabled) {
            drawShootingStars(
                shootingStars = shootingStars,
                timeSec = timeSec,
                w = w,
                h = h,
                intensity = intensity
            )
        }

        // 5. 3D Cyberpunk Möbius Ribbon Engine
        if (showRibbon) {
            val ribbonCenterY = cy + ribbonOffsetY * minDim
            val computedScale = (minDim / 380f).coerceIn(0.6f, 1.7f) * ribbonScale

            drawCyberpunkMobiusRibbon(
                state = mobiusState,
                cx = cx,
                cy = ribbonCenterY,
                scale = computedScale,
                timeSec = timeSec,
                intensity = intensity,
                showOrbitalRings = showOrbitalRings,
                showCenterCore = showCenterCore,
                reduceMotion = reduceMotion
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Draw Multi-Layer Deep Space Starfield
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawDeepSpaceStarfield(
    stars: Array<StarParticle>,
    timeSec: Float,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    camZ: Float,
    fov: Float,
    ws: Float,
    intensity: Float
) {
    val refScale = fov / camZ

    for (s in stars) {
        val rawX = s.xNorm + s.vxNorm * timeSec
        val rawY = s.yNorm + s.vyNorm * timeSec
        val rawZ = s.zNorm + s.vzNorm * timeSec

        // Seamless infinite wrap around [-1, 1]
        val px = ((rawX + 5f) % 2f - 1f) * (w * 0.65f)
        val py = ((rawY + 5f) % 2f - 1f) * (h * 0.65f)
        val pz = ((rawZ + 5f) % 2f - 1f) * 450f * ws

        val depth = pz + camZ
        if (depth < 20f) continue

        val sc = fov / depth
        val sx = cx + px * sc
        val sy = cy + py * sc

        if (sx < -40f || sx > w + 40f || sy < -40f || sy > h + 40f) continue

        val relScale = sc / refScale
        val radius = (s.size * relScale * ws).coerceIn(0.5f, 6.5f * ws)

        // Shimmer twinkle
        val twinkle = sin(timeSec * s.twinkleSpeed + s.twinklePhase) * 0.16f
        val opacity = ((s.baseAlpha * relScale.coerceIn(0.3f, 1.8f)) + twinkle).coerceIn(0.04f, 0.95f) * intensity

        val starColor = when (s.starType) {
            1 -> Color(0xFF38BDF8).copy(alpha = opacity) // Cyan
            2 -> Color(0xFFA855F7).copy(alpha = opacity) // Violet
            3 -> Color(0xFFFBBF24).copy(alpha = opacity) // Gold
            else -> Color(0xFFF1F5F9).copy(alpha = opacity) // Silver-white
        }

        drawCircle(color = starColor, radius = radius, center = Offset(sx, sy))

        // Large bright stars get soft glow halo
        if (relScale > 1.3f && radius > 2.2f * ws) {
            drawCircle(
                color = starColor.copy(alpha = (opacity * 0.3f).coerceAtMost(0.3f)),
                radius = radius * 2.5f,
                center = Offset(sx, sy)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Draw Shooting Stars (Energetic Meteors)
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawShootingStars(
    shootingStars: Array<ShootingStar>,
    timeSec: Float,
    w: Float,
    h: Float,
    intensity: Float
) {
    for (i in shootingStars.indices) {
        val s = shootingStars[i]
        val cycle = (timeSec * 0.35f + i * 2.3f) % 4.5f

        if (cycle < 1.2f) {
            val progress = cycle / 1.2f
            val startX = (w * 0.1f) + (i * w * 0.35f)
            val startY = (h * 0.05f) + (i * h * 0.2f)
            val length = 140f
            val angle = 0.65f // radians (~37 degrees diagonal)

            val curX = startX + progress * w * 0.55f
            val curY = startY + progress * h * 0.45f
            val tailX = curX - cos(angle) * length * (1f - progress * 0.3f)
            val tailY = curY - sin(angle) * length * (1f - progress * 0.3f)

            val alpha = (sin(progress * PI.toFloat()) * 0.75f * intensity).coerceIn(0f, 1f)
            if (alpha > 0.02f) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color(0xFF818CF8).copy(alpha = alpha), Color.White.copy(alpha = alpha)),
                        start = Offset(tailX, tailY),
                        end = Offset(curX, curY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(curX, curY),
                    strokeWidth = 2.0f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
