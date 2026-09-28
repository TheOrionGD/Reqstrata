package com.theoriongd.reqstrata.ui.components.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.theoriongd.reqstrata.ui.motion.LocalReduceMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Motion constants — centralized, never scattered through renderer
// ─────────────────────────────────────────────────────────────────────────────
internal object SpaceMotionConstants {
    /** Seconds for one full visual Y-rotation of the Mobius strip */
    const val MobiusRotationPeriodSec = 30f
    /** Twinkle oscillation rate multiplier */
    const val ParticleTwinkleSpeed = 0.7f
    /** Mobius mesh resolution (u = major ring direction) */
    const val MobiusUSteps = 72
    /** Mobius mesh resolution (t = strip-width direction) */
    const val MobiusTSteps = 8
    /** Fixed X-axis tilt for the Mobius ring (radians ~24 deg) */
    const val MobiusTiltAngle = 0.42f
    /** Additional slow Y-bob frequency (Hz) */
    const val MobiusBobFreqHz = 0.04f
    /** Atmosphere glow: fraction of minDimension */
    const val AtmosphereRadiusFraction = 0.48f
}

// ─────────────────────────────────────────────────────────────────────────────
// Particle data — stable, initialized once per particleCount change
// All positions normalized to [-1, 1]; scaled to pixel space at render time
// ─────────────────────────────────────────────────────────────────────────────
internal class SpaceParticle(
    val xNorm: Float,       // [-1, 1]
    val yNorm: Float,       // [-1, 1]
    val zNorm: Float,       // [-1, 1]
    val vxNorm: Float,      // drift velocity in normalized units/sec
    val vyNorm: Float,
    val vzNorm: Float,
    val baseSize: Float,    // base radius in reference pixels
    val baseOpacity: Float, // [0.06, 0.70]
    val twinkleFreq: Float, // Hz
    val twinklePhase: Float,// radians
    val colorType: Int      // 0=neutral, 1=violet, 2=amber, 3=emerald
)

// ─────────────────────────────────────────────────────────────────────────────
// Mobius body-space vertex (R=1 normalized, W=0.33)
// ─────────────────────────────────────────────────────────────────────────────
internal class MobiusBodyVertex(val x: Float, val y: Float, val z: Float)

// ─────────────────────────────────────────────────────────────────────────────
// Generate stable particle array
// ─────────────────────────────────────────────────────────────────────────────
private fun generateParticles(count: Int): Array<SpaceParticle> {
    val rng = kotlin.random.Random(0x4A7BC1F3.toInt())
    return Array(count) {
        val colorRoll = rng.nextFloat()
        SpaceParticle(
            xNorm = rng.nextFloat() * 2f - 1f,
            yNorm = rng.nextFloat() * 2f - 1f,
            zNorm = rng.nextFloat() * 2f - 1f,
            // Very slow drift — full normalized range in ~60-120 seconds
            vxNorm = (rng.nextFloat() - 0.5f) * 0.015f,
            vyNorm = (rng.nextFloat() - 0.5f) * 0.012f,
            vzNorm = (rng.nextFloat() - 0.5f) * 0.018f,
            baseSize = rng.nextFloat() * 3.2f + 0.6f,
            baseOpacity = rng.nextFloat() * 0.52f + 0.07f,
            twinkleFreq = rng.nextFloat() * 1.1f + 0.22f,
            twinklePhase = rng.nextFloat() * 2f * PI.toFloat(),
            colorType = when {
                colorRoll < 0.81f -> 0  // neutral grey/white
                colorRoll < 0.91f -> 1  // violet
                colorRoll < 0.96f -> 2  // amber
                else              -> 3  // emerald
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Generate Mobius body-space vertices (normalized: R=1, W=0.33)
// Parameterization:
//   x = (R + t*cos(u/2)) * cos(u)
//   y = (R + t*cos(u/2)) * sin(u)
//   z = t * sin(u/2)
// ─────────────────────────────────────────────────────────────────────────────
private fun generateMobiusBody(uSteps: Int, tSteps: Int): Array<Array<MobiusBodyVertex>> {
    val R = 1.0f
    val W = 0.33f
    return Array(uSteps + 1) { ui ->
        val u = (ui.toFloat() / uSteps.toFloat()) * 2f * PI.toFloat()
        val halfU    = u * 0.5f
        val cosHalfU = cos(halfU)
        val sinHalfU = sin(halfU)
        val cosU     = cos(u)
        val sinU     = sin(u)
        Array(tSteps + 1) { ti ->
            val t = -W + (ti.toFloat() / tSteps.toFloat()) * 2f * W
            val r = R + t * cosHalfU
            MobiusBodyVertex(x = r * cosU, y = r * sinU, z = t * sinHalfU)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MobiusSpaceBackground — the public composable
//
//  particleCount  : target particle count (clamped to [40, 400])
//  rotationSpeed  : multiplier on the base 30s rotation period
//  intensity      : overall visual weight [0f = invisible, 1f = full]
//  enabled        : when false, draws a static frozen snapshot (timeSec=0.5)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MobiusSpaceBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 200,
    rotationSpeed: Float = 1f,
    intensity: Float = 1f,
    enabled: Boolean = true
) {
    val reduceMotion = LocalReduceMotion.current
    val uSteps = SpaceMotionConstants.MobiusUSteps
    val tSteps = SpaceMotionConstants.MobiusTSteps

    // Stable particle data — recreated only when particleCount changes
    val particles  = remember(particleCount) { generateParticles(particleCount.coerceIn(40, 400)) }
    // Stable Mobius body-space vertices
    val mobiusBody = remember(uSteps, tSteps) { generateMobiusBody(uSteps, tSteps) }

    // ── Scratch buffers — pre-allocated, reused every frame (no heap churn) ──
    val totalVerts = (uSteps + 1) * (tSteps + 1)
    val totalQuads = uSteps * tSteps
    val scrX       = remember { FloatArray(totalVerts) }
    val scrY       = remember { FloatArray(totalVerts) }
    val scrZ       = remember { FloatArray(totalVerts) }
    val quadDepths = remember { FloatArray(totalQuads) }
    val quadFacing = remember { FloatArray(totalQuads) }
    // MutableList reused across frames (ArrayList grows once then stabilizes)
    val sortedIdxs = remember { mutableListOf<Int>() }
    // Pre-allocated Path object, reset() on each quad
    val quadPath   = remember { Path() }

    // ── Animation time ────────────────────────────────────────────────────────
    // A single Long state drives ALL animation — exactly one Canvas recomposition
    // per frame when the animation is running.
    var timeNanos  by remember { mutableLongStateOf(0L) }
    var startNanos by remember { mutableLongStateOf(-1L) }

    // Animation loop — auto-cancelled when composable leaves composition
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

    // ── Single Canvas layer — all 3D geometry drawn here ─────────────────────
    Canvas(modifier = modifier.fillMaxSize()) {
        // Time in seconds; frozen at 0.5s for a nice static snapshot when paused
        val timeSec = if (reduceMotion || !enabled) 0.5f else timeNanos / 1_000_000_000f

        val cx = size.width  * 0.5f
        val cy = size.height * 0.5f

        // World scale: 1.0 at 1000px minimum dimension
        val ws = (size.minDimension / 1000f).coerceAtLeast(0.3f)

        // ── Projection parameters (world pixel units) ─────────────────────────
        val mobiusRadius = 210f * ws     // major ring radius
        val camZ         = 780f * ws     // camera-to-origin distance
        val fov          = 920f * ws     // focal length (perspective strength)

        // ── Particle world extents ────────────────────────────────────────────
        val pRX = size.width  * 0.62f
        val pRY = size.height * 0.62f
        val pRZ = 380f * ws

        // ── Mobius rotation: slow Y-rotation + subtle X-tilt bob ─────────────
        val tau      = 2f * PI.toFloat()
        val rotAngle = timeSec * tau / (SpaceMotionConstants.MobiusRotationPeriodSec / rotationSpeed)
        val bobAngle = SpaceMotionConstants.MobiusTiltAngle +
                       sin(timeSec * SpaceMotionConstants.MobiusBobFreqHz * tau) * 0.06f

        val cosR = cos(rotAngle); val sinR = sin(rotAngle)
        val cosT = cos(bobAngle); val sinT = sin(bobAngle)

        // ── 1. Background fill ────────────────────────────────────────────────
        drawRect(color = Color(0xFF09090B))

        // ── 2. Atmospheric depth glow (very subtle volumetric center) ─────────
        val atmoR = size.minDimension * SpaceMotionConstants.AtmosphereRadiusFraction
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xFF6D28D9).copy(alpha = 0.050f * intensity),
                    0.45f to Color(0xFF4C1D95).copy(alpha = 0.025f * intensity),
                    0.80f to Color(0xFF1C0A3E).copy(alpha = 0.010f * intensity),
                    1.00f to Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = atmoR
            ),
            radius = atmoR,
            center = Offset(cx, cy)
        )

        // ── 3. Project Mobius body-space vertices to screen space ─────────────
        for (ui in 0..uSteps) {
            for (ti in 0..tSteps) {
                val v  = mobiusBody[ui][ti]
                // Scale normalized body coords → world pixels
                val bx = v.x * mobiusRadius
                val by = v.y * mobiusRadius
                val bz = v.z * mobiusRadius

                // Rotate around Y axis
                val rx = bx * cosR + bz * sinR
                val ry = by
                val rz = -bx * sinR + bz * cosR

                // Tilt around X axis (with gentle bob)
                val tx = rx
                val ty = ry * cosT - rz * sinT
                val tz = ry * sinT + rz * cosT

                val depth = tz + camZ
                val idx   = ui * (tSteps + 1) + ti

                if (depth > 5f) {
                    val sc = fov / depth
                    scrX[idx] = cx + tx * sc
                    scrY[idx] = cy + ty * sc
                } else {
                    scrX[idx] = Float.NaN   // degenerate; culled in draw step
                    scrY[idx] = Float.NaN
                }
                scrZ[idx] = tz
            }
        }

        // ── 4. Compute quad depths and screen-space facing ────────────────────
        for (ui in 0 until uSteps) {
            for (ti in 0 until tSteps) {
                val qi  = ui * tSteps + ti
                val i00 =  ui      * (tSteps + 1) + ti
                val i10 = (ui + 1) * (tSteps + 1) + ti
                val i11 = (ui + 1) * (tSteps + 1) + ti + 1
                val i01 =  ui      * (tSteps + 1) + ti + 1

                quadDepths[qi] = (scrZ[i00] + scrZ[i10] + scrZ[i11] + scrZ[i01]) * 0.25f

                // 2D cross product (ex1 x ey2 - ey1 x ex2):
                // Negative in Compose y-down coords = CCW wound = front-facing
                val ex1 = scrX[i10] - scrX[i00]; val ey1 = scrY[i10] - scrY[i00]
                val ex2 = scrX[i01] - scrX[i00]; val ey2 = scrY[i01] - scrY[i00]
                quadFacing[qi] = ex1 * ey2 - ey1 * ex2
            }
        }

        // ── 5. Sort quads back-to-front (painter's algorithm) ─────────────────
        sortedIdxs.clear()
        repeat(totalQuads) { sortedIdxs.add(it) }
        sortedIdxs.sortByDescending { quadDepths[it] }

        val refScale = fov / camZ  // projection scale at world origin

        // ── 6. Draw particles (behind Mobius; visible through semi-transparent surface)
        drawSpaceParticles(
            particles = particles,
            timeSec   = timeSec,
            cx = cx, cy = cy,
            pRX = pRX, pRY = pRY, pRZ = pRZ,
            camZ = camZ, fov = fov, refScale = refScale,
            intensity = intensity,
            screenW = size.width, screenH = size.height,
            ws = ws
        )

        // ── 7. Draw Mobius quads back-to-front ────────────────────────────────
        for (qi in sortedIdxs) {
            val ui = qi / tSteps
            val ti = qi % tSteps

            val i00 =  ui      * (tSteps + 1) + ti
            val i10 = (ui + 1) * (tSteps + 1) + ti
            val i11 = (ui + 1) * (tSteps + 1) + ti + 1
            val i01 =  ui      * (tSteps + 1) + ti + 1

            val sx0 = scrX[i00]; val sy0 = scrY[i00]
            val sx1 = scrX[i10]; val sy1 = scrY[i10]
            val sx2 = scrX[i11]; val sy2 = scrY[i11]
            val sx3 = scrX[i01]; val sy3 = scrY[i01]

            // Skip degenerate quads
            if (sx0.isNaN() || sx1.isNaN() || sx2.isNaN() || sx3.isNaN()) continue

            // Broad frustum cull
            val margin = 200f
            if (maxOf(sx0, sx1, sx2, sx3) < -margin) continue
            if (minOf(sx0, sx1, sx2, sx3) > size.width  + margin) continue
            if (maxOf(sy0, sy1, sy2, sy3) < -margin) continue
            if (minOf(sy0, sy1, sy2, sy3) > size.height + margin) continue

            val isFront = quadFacing[qi] < 0f
            val isEdge  = ti == 0 || ti == tSteps - 1

            // Depth-based brightness (closer = brighter)
            val depth       = quadDepths[qi] + camZ
            val depthBright = if (depth > 0f) (camZ / depth).coerceIn(0.55f, 1.45f) else 1f

            // Fill alpha: front = opaque, back = ghostly
            val baseFill  = if (isFront) 0.42f else 0.16f
            val edgeAdd   = if (isEdge && isFront) 0.12f else 0f
            val fillAlpha = ((baseFill + edgeAdd) * depthBright * intensity).coerceIn(0f, 0.72f)

            val fillColor = if (isFront) {
                Color(0xFF3D1A80).copy(alpha = fillAlpha)
            } else {
                Color(0xFF180830).copy(alpha = fillAlpha)
            }

            quadPath.reset()
            quadPath.moveTo(sx0, sy0)
            quadPath.lineTo(sx1, sy1)
            quadPath.lineTo(sx2, sy2)
            quadPath.lineTo(sx3, sy3)
            quadPath.close()

            drawPath(quadPath, fillColor)

            // Edge highlight strokes (edge quads = rim of the strip)
            val strokeAlpha = if (isEdge) {
                (0.65f * depthBright * intensity).coerceIn(0f, 0.90f)
            } else {
                (0.09f * depthBright * intensity).coerceIn(0f, 0.22f)
            }
            if (strokeAlpha > 0.01f) {
                val strokeColor = if (isEdge) {
                    Color(0xFFA78BFA).copy(alpha = strokeAlpha)
                } else {
                    Color(0xFF8B5CF6).copy(alpha = strokeAlpha)
                }
                val strokeW = (if (isEdge) 1.3f else 0.55f) * ws
                drawPath(quadPath, strokeColor, style = Stroke(width = strokeW))
            }
        }

        // ── 8. Secondary atmosphere ring (subtle haze around the structure) ───
        val ringR = mobiusRadius * 1.55f
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to Color.Transparent,
                    0.65f to Color(0xFF5B21B6).copy(alpha = 0.018f * intensity),
                    0.90f to Color(0xFF4C1D95).copy(alpha = 0.012f * intensity),
                    1.00f to Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = ringR
            ),
            radius = ringR,
            center = Offset(cx, cy)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private extension: render particle field inside a DrawScope
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawSpaceParticles(
    particles : Array<SpaceParticle>,
    timeSec   : Float,
    cx: Float, cy: Float,
    pRX: Float, pRY: Float, pRZ: Float,
    camZ     : Float,
    fov      : Float,
    refScale : Float,
    intensity: Float,
    screenW  : Float,
    screenH  : Float,
    ws       : Float
) {
    val twinkT = timeSec * SpaceMotionConstants.ParticleTwinkleSpeed

    for (p in particles) {
        // Drift position: continuous normalized coordinates
        val rawX = p.xNorm + p.vxNorm * timeSec
        val rawY = p.yNorm + p.vyNorm * timeSec
        val rawZ = p.zNorm + p.vzNorm * timeSec

        // Wrap into [-1, 1] smoothly (no visible pop)
        val px = ((rawX + 5f) % 2f - 1f) * pRX
        val py = ((rawY + 5f) % 2f - 1f) * pRY
        val pz = ((rawZ + 5f) % 2f - 1f) * pRZ

        val depth = pz + camZ
        if (depth < 30f) continue          // behind camera

        val sc = fov / depth
        val sx = cx + px * sc
        val sy = cy + py * sc

        // Broad frustum cull
        val cm = 60f
        if (sx < -cm || sx > screenW + cm || sy < -cm || sy > screenH + cm) continue

        // Relative depth scale (1.0 at world origin, >1 = closer)
        val relScale = sc / refScale

        val radius = (p.baseSize * relScale * ws).coerceIn(0.4f, 7f * ws)

        // Subtle twinkle: sin-based, very small amplitude
        val twinkle = sin(twinkT * p.twinkleFreq + p.twinklePhase) * 0.10f

        // Opacity: brighter when closer, dimmer when far
        val opacity = ((p.baseOpacity * relScale.coerceIn(0.25f, 1.8f)) + twinkle)
            .coerceIn(0.02f, 0.88f) * intensity

        val color = when (p.colorType) {
            1    -> Color(0xFF8B5CF6).copy(alpha = opacity)
            2    -> Color(0xFFF59E0B).copy(alpha = opacity * 0.72f)
            3    -> Color(0xFF34D399).copy(alpha = opacity * 0.65f)
            else -> Color(0xFFD4D4D8).copy(alpha = opacity * 0.80f)
        }

        drawCircle(color = color, radius = radius, center = Offset(sx, sy))

        // Soft glow halo on larger near particles for visual depth weight
        if (relScale > 1.2f && radius > 2f * ws) {
            val glowAlpha = (opacity * 0.25f).coerceAtMost(0.25f)
            drawCircle(
                color  = color.copy(alpha = glowAlpha),
                radius = radius * 2.2f,
                center = Offset(sx, sy)
            )
        }
    }
}
