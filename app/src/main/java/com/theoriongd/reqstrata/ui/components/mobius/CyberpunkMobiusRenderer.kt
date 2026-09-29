package com.theoriongd.reqstrata.ui.components.mobius

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// Production Cyberpunk 3D Möbius Ribbon Engine
// Continuous toroidal-elliptical topology, cybernetic matrix grid,
// embedded micro-nodes, multi-pass luminous bloom, and 3D orbital rings.
// ─────────────────────────────────────────────────────────────────────────────

private const val U_STEPS = 120         // Smooth continuous loop along ribbon length
private const val T_STEPS = 10          // Longitudinal grid resolution across ribbon width
private const val TOTAL_VERTS = (U_STEPS + 1) * (T_STEPS + 1)
private const val TOTAL_QUADS = U_STEPS * T_STEPS
private const val PARTICLE_COUNT = 48   // Continuous lifecycle data particles
private const val ORBITAL_NODE_COUNT = 7

class CyberpunkRibbonDataParticle(
    var uNorm: Float,       // [0, 1] loop position
    var tNorm: Float,       // [-1, 1] lateral offset across width
    val speed: Float,       // speed multiplier
    val radius: Float,      // dot radius
    val colorType: Int      // 0=Cyan, 1=Violet, 2=Magenta, 3=White
)

class CyberpunkOrbitalNode(
    val orbitIndex: Int,    // 0, 1, or 2
    var angleNorm: Float,   // [0, 1] along ellipse
    val speed: Float,
    val size: Float,
    val color: Color
)

@Stable
class CyberpunkMobiusState internal constructor(
    val scrX: FloatArray,
    val scrY: FloatArray,
    val scrZ: FloatArray,
    val quadDepths: FloatArray,
    val quadFacing: FloatArray,
    val sortedQuads: ArrayList<Int>,
    val quadPath: Path,
    val linePath: Path,
    val edgePath: Path,
    val badgePath: Path,
    val checkPath: Path,
    val particles: Array<CyberpunkRibbonDataParticle>,
    val orbitalNodes: Array<CyberpunkOrbitalNode>
)

@Composable
fun rememberCyberpunkMobiusState(): CyberpunkMobiusState {
    return remember {
        val scrX = FloatArray(TOTAL_VERTS)
        val scrY = FloatArray(TOTAL_VERTS)
        val scrZ = FloatArray(TOTAL_VERTS)
        val quadDepths = FloatArray(TOTAL_QUADS)
        val quadFacing = FloatArray(TOTAL_QUADS)
        val sortedQuads = ArrayList<Int>(TOTAL_QUADS).apply {
            repeat(TOTAL_QUADS) { add(it) }
        }

        val quadPath = Path()
        val linePath = Path()
        val edgePath = Path()
        val badgePath = Path()
        val checkPath = Path()

        val rng = kotlin.random.Random(0x4D6F6269)
        val particles = Array(PARTICLE_COUNT) { i ->
            CyberpunkRibbonDataParticle(
                uNorm = i.toFloat() / PARTICLE_COUNT.toFloat(),
                tNorm = (rng.nextFloat() * 2f - 1f) * 0.85f,
                speed = 0.045f + rng.nextFloat() * 0.055f,
                radius = 1.4f + rng.nextFloat() * 2.2f,
                colorType = rng.nextInt(4)
            )
        }

        val colors = listOf(
            Color(0xFF00F0FF), // Electric Neon Cyan
            Color(0xFFA855F7), // Neon Violet
            Color(0xFFEC4899), // Hot Cyberpunk Magenta
            Color(0xFF6366F1), // Electric Indigo
            Color(0xFF22D3EE), // Bright Aqua
            Color(0xFFF43F5E), // Laser Pink
            Color(0xFFC084FC)  // Soft Lilac Neon
        )
        val orbitalNodes = Array(ORBITAL_NODE_COUNT) { i ->
            CyberpunkOrbitalNode(
                orbitIndex = i % 3,
                angleNorm = i.toFloat() / ORBITAL_NODE_COUNT.toFloat(),
                speed = 0.025f + (i * 0.007f),
                size = 3.2f + (i % 3) * 1.2f,
                color = colors[i % colors.size]
            )
        }

        CyberpunkMobiusState(
            scrX = scrX,
            scrY = scrY,
            scrZ = scrZ,
            quadDepths = quadDepths,
            quadFacing = quadFacing,
            sortedQuads = sortedQuads,
            quadPath = quadPath,
            linePath = linePath,
            edgePath = edgePath,
            badgePath = badgePath,
            checkPath = checkPath,
            particles = particles,
            orbitalNodes = orbitalNodes
        )
    }
}

/**
 * Continuous high-contrast cyberpunk color evaluation along ribbon length.
 * Cyan -> Aqua/Teal -> Electric Indigo -> Neon Violet -> Hot Magenta -> Cyan
 */
fun evaluateCyberpunkRibbonColor(uRatio: Float): Color {
    val u = (uRatio % 1.0f + 1.0f) % 1.0f
    return when {
        u < 0.22f -> {
            // Neon Cyan to Aqua Teal
            val t = u / 0.22f
            lerpCyberpunkColor(Color(0xFF00F0FF), Color(0xFF06B6D4), t)
        }
        u < 0.45f -> {
            // Aqua Teal to Electric Indigo
            val t = (u - 0.22f) / 0.23f
            lerpCyberpunkColor(Color(0xFF06B6D4), Color(0xFF4338CA), t)
        }
        u < 0.70f -> {
            // Electric Indigo to Vivid Violet
            val t = (u - 0.45f) / 0.25f
            lerpCyberpunkColor(Color(0xFF4338CA), Color(0xFFA855F7), t)
        }
        u < 0.88f -> {
            // Vivid Violet to Hot Cyberpunk Magenta
            val t = (u - 0.70f) / 0.18f
            lerpCyberpunkColor(Color(0xFFA855F7), Color(0xFFEC4899), t)
        }
        else -> {
            // Hot Magenta returning to Neon Cyan
            val t = (u - 0.88f) / 0.12f
            lerpCyberpunkColor(Color(0xFFEC4899), Color(0xFF00F0FF), t)
        }
    }
}

private fun lerpCyberpunkColor(c1: Color, c2: Color, t: Float): Color {
    val ct = t.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * ct,
        green = c1.green + (c2.green - c1.green) * ct,
        blue = c1.blue + (c2.blue - c1.blue) * ct,
        alpha = 1.0f
    )
}

/**
 * High-performance 3D Cyberpunk Möbius Ribbon Draw Function.
 * Zero allocation per frame.
 */
fun DrawScope.drawCyberpunkMobiusRibbon(
    state: CyberpunkMobiusState,
    cx: Float,
    cy: Float,
    scale: Float,
    timeSec: Float,
    intensity: Float = 1.0f,
    showOrbitalRings: Boolean = true,
    showCenterCore: Boolean = false,
    pageIndex: Int = 2,
    badgeIcon: ImageVector? = null,
    reduceMotion: Boolean = false
) {
    if (intensity <= 0.01f) return

    val majorR = 112f * scale
    val ribbonW = 0.44f // width factor

    // Camera perspective parameters
    val camZ = 640f * scale
    val fov = 750f * scale

    // Slow, elegant 3D continuous rotation (yaw, pitch, roll)
    val yaw = if (reduceMotion) 0.35f else (timeSec * 0.26f)
    val pitch = 0.44f + (if (reduceMotion) 0f else sin(timeSec * 0.16f) * 0.06f)
    val roll = -0.14f + (if (reduceMotion) 0f else cos(timeSec * 0.12f) * 0.04f)

    val cosY = cos(yaw); val sinY = sin(yaw)
    val cosP = cos(pitch); val sinP = sin(pitch)
    val cosR = cos(roll); val sinR = sin(roll)

    // ─────────────────────────────────────────────────────────────────────
    // 1. COMPUTE 3D MÖBIUS VERTICES & PERSPECTIVE PROJECTION
    // ─────────────────────────────────────────────────────────────────────
    val twoPi = (2f * PI).toFloat()
    val rx = 1.25f * majorR
    val ry = 0.86f * majorR

    for (ui in 0..U_STEPS) {
        val u = (ui.toFloat() / U_STEPS.toFloat()) * twoPi
        val halfU = u * 0.5f
        val cosHalfU = cos(halfU)
        val sinHalfU = sin(halfU)
        val cosU = cos(u)
        val sinU = sin(u)

        for (ti in 0..T_STEPS) {
            val tNorm = -ribbonW + (ti.toFloat() / T_STEPS.toFloat()) * 2f * ribbonW
            val r = 1.0f + tNorm * cosHalfU

            // Body coordinates with half-twist along Z
            val bx = r * cosU * rx
            val by = r * sinU * ry
            val bz = tNorm * sinHalfU * majorR * 1.62f

            // 3D Rotation: Yaw (Y) -> Pitch (X) -> Roll (Z)
            val x1 = bx * cosY + bz * sinY
            val y1 = by
            val z1 = -bx * sinY + bz * cosY

            val x2 = x1
            val y2 = y1 * cosP - z1 * sinP
            val z2 = y1 * sinP + z1 * cosP

            val x3 = x2 * cosR - y2 * sinR
            val y3 = x2 * sinR + y2 * cosR
            val z3 = z2

            val depth = z3 + camZ
            val idx = ui * (T_STEPS + 1) + ti

            if (depth > 20f) {
                val proj = fov / depth
                state.scrX[idx] = cx + x3 * proj
                state.scrY[idx] = cy + y3 * proj
            } else {
                state.scrX[idx] = Float.NaN
                state.scrY[idx] = Float.NaN
            }
            state.scrZ[idx] = z3
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. COMPUTE QUAD DEPTHS & SURFACE NORMALS
    // ─────────────────────────────────────────────────────────────────────
    for (ui in 0 until U_STEPS) {
        for (ti in 0 until T_STEPS) {
            val qi = ui * T_STEPS + ti
            val i00 = ui * (T_STEPS + 1) + ti
            val i10 = (ui + 1) * (T_STEPS + 1) + ti
            val i11 = (ui + 1) * (T_STEPS + 1) + ti + 1
            val i01 = ui * (T_STEPS + 1) + ti + 1

            state.quadDepths[qi] = (state.scrZ[i00] + state.scrZ[i10] + state.scrZ[i11] + state.scrZ[i01]) * 0.25f

            val ex1 = state.scrX[i10] - state.scrX[i00]
            val ey1 = state.scrY[i10] - state.scrY[i00]
            val ex2 = state.scrX[i01] - state.scrX[i00]
            val ey2 = state.scrY[i01] - state.scrY[i00]
            state.quadFacing[qi] = ex1 * ey2 - ey1 * ex2
        }
    }

    // Sort back-to-front for proper translucent depth blending
    state.sortedQuads.sortByDescending { state.quadDepths[it] }

    // ─────────────────────────────────────────────────────────────────────
    // 3. DRAW AMBIENT CYBERPUNK RADIAL GLOW & BACK-HALF ORBITAL RINGS
    // ─────────────────────────────────────────────────────────────────────
    drawAmbientCyberpunkGlow(cx = cx, cy = cy, radius = majorR * 1.55f, intensity = intensity)

    if (showOrbitalRings) {
        drawOrbitalTrajectories(
            cx = cx,
            cy = cy,
            majorR = majorR,
            scale = scale,
            intensity = intensity,
            drawBackHalf = true
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. RENDER TRANSLUCENT 3D MÖBIUS RIBBON QUADS (DEPTH-SORTED)
    // ─────────────────────────────────────────────────────────────────────
    for (qi in state.sortedQuads) {
        val ui = qi / T_STEPS
        val ti = qi % T_STEPS

        val i00 = ui * (T_STEPS + 1) + ti
        val i10 = (ui + 1) * (T_STEPS + 1) + ti
        val i11 = (ui + 1) * (T_STEPS + 1) + ti + 1
        val i01 = ui * (T_STEPS + 1) + ti + 1

        val x0 = state.scrX[i00]; val y0 = state.scrY[i00]
        val x1 = state.scrX[i10]; val y1 = state.scrY[i10]
        val x2 = state.scrX[i11]; val y2 = state.scrY[i11]
        val x3 = state.scrX[i01]; val y3 = state.scrY[i01]

        if (x0.isNaN() || x1.isNaN() || x2.isNaN() || x3.isNaN()) continue

        val isFront = state.quadFacing[qi] < 0f
        val isEdge = (ti == 0 || ti == T_STEPS - 1)
        val zAvg = state.quadDepths[qi]

        // Depth brightness factor
        val depthFactor = ((zAvg + camZ * 0.45f) / (camZ * 0.9f)).coerceIn(0.35f, 1.55f)

        // Normalized loop ratio along ribbon
        val uRatio = ui.toFloat() / U_STEPS.toFloat()

        // Evaluated holographic color
        val surfaceColor = evaluateCyberpunkRibbonColor(uRatio)

        val baseAlpha = if (isFront) 0.36f else 0.14f
        val edgeBonus = if (isEdge) 0.20f else 0.0f
        val quadAlpha = ((baseAlpha + edgeBonus) * depthFactor * intensity).coerceIn(0.05f, 0.85f)

        state.quadPath.reset()
        state.quadPath.moveTo(x0, y0)
        state.quadPath.lineTo(x1, y1)
        state.quadPath.lineTo(x2, y2)
        state.quadPath.lineTo(x3, y3)
        state.quadPath.close()

        // Translucent holographic quad fill
        drawPath(path = state.quadPath, color = surfaceColor.copy(alpha = quadAlpha))
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. DRAW CURVILINEAR CYBERNETIC GRID & EMBEDDED MICRO-NODES
    // ─────────────────────────────────────────────────────────────────────
    // A. Longitudinal cybernetic lines along ribbon length (following surface curvature)
    val scanPulse = if (reduceMotion) 0.5f else ((timeSec * 0.18f) % 1.0f)
    for (ti in 0..T_STEPS step 2) {
        val isOuterBorder = (ti == 0 || ti == T_STEPS)
        state.linePath.reset()
        var started = false

        for (ui in 0..U_STEPS) {
            val idx = ui * (T_STEPS + 1) + ti
            val px = state.scrX[idx]
            val py = state.scrY[idx]
            if (!px.isNaN() && !py.isNaN()) {
                if (!started) {
                    state.linePath.moveTo(px, py)
                    started = true
                } else {
                    state.linePath.lineTo(px, py)
                }
            }
        }

        val gridAlpha = (if (isOuterBorder) 0.75f else 0.35f) * intensity
        val gridColor = if (ti <= 3) Color(0xFF00F0FF) else if (ti >= 7) Color(0xFFA855F7) else Color(0xFFEC4899)
        drawPath(
            path = state.linePath,
            color = gridColor.copy(alpha = gridAlpha.coerceIn(0.05f, 0.95f)),
            style = Stroke(width = if (isOuterBorder) 1.8f * scale else 0.95f * scale, cap = StrokeCap.Round)
        )
    }

    // B. Transverse ribs & Embedded Micro-particle intersections
    for (ui in 0..U_STEPS step 3) {
        val uRatio = ui.toFloat() / U_STEPS.toFloat()
        val ribColor = evaluateCyberpunkRibbonColor(uRatio)

        // Dynamic scanning wave bonus
        val distToScan = abs(uRatio - scanPulse)
        val scanGlow = if (distToScan < 0.08f) (1f - distToScan / 0.08f) * 0.55f else 0f

        val idxStart = ui * (T_STEPS + 1)
        val idxEnd = ui * (T_STEPS + 1) + T_STEPS

        val sx = state.scrX[idxStart]; val sy = state.scrY[idxStart]
        val ex = state.scrX[idxEnd]; val ey = state.scrY[idxEnd]

        if (!sx.isNaN() && !ex.isNaN()) {
            val lineAlpha = ((0.24f + scanGlow) * intensity).coerceIn(0.05f, 0.90f)
            drawLine(
                color = ribColor.copy(alpha = lineAlpha),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = 0.85f * scale
            )
        }

        // Embedded micro-nodes at grid intersections (sparkling digital matrix)
        for (ti in 1 until T_STEPS step 3) {
            val pIdx = ui * (T_STEPS + 1) + ti
            val mx = state.scrX[pIdx]; val my = state.scrY[pIdx]; val mz = state.scrZ[pIdx]
            if (!mx.isNaN()) {
                val nodeDepthFactor = ((mz + camZ * 0.4f) / (camZ * 0.8f)).coerceIn(0.35f, 1.45f)
                val nodeAlpha = ((0.42f + scanGlow) * nodeDepthFactor * intensity).coerceIn(0.08f, 0.98f)

                // White core dot
                drawCircle(
                    color = Color.White.copy(alpha = nodeAlpha),
                    radius = (1.0f * scale),
                    center = Offset(mx, my)
                )
                // Colored micro aura
                if (scanGlow > 0.1f) {
                    drawCircle(
                        color = Color(0xFF00F0FF).copy(alpha = nodeAlpha * 0.45f),
                        radius = (2.2f * scale),
                        center = Offset(mx, my)
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 6. MULTI-PASS LUMINOUS NEON BLOOM EDGES
    // ─────────────────────────────────────────────────────────────────────
    drawMultiPassNeonEdges(
        state = state,
        camZ = camZ,
        scale = scale,
        timeSec = timeSec,
        intensity = intensity,
        reduceMotion = reduceMotion
    )

    // ─────────────────────────────────────────────────────────────────────
    // 7. CONTINUOUS LIFECYCLE DATA PARTICLES (Requirements -> Validation)
    // ─────────────────────────────────────────────────────────────────────
    for (p in state.particles) {
        if (!reduceMotion) {
            p.uNorm = (p.uNorm + p.speed * 0.016f) % 1.0f
        }

        val ui = (p.uNorm * U_STEPS).toInt().coerceIn(0, U_STEPS)
        val tiFloat = ((p.tNorm + ribbonW) / (2f * ribbonW)) * T_STEPS
        val ti = tiFloat.toInt().coerceIn(0, T_STEPS)

        val idx = ui * (T_STEPS + 1) + ti
        val px = state.scrX[idx]
        val py = state.scrY[idx]
        val pz = state.scrZ[idx]

        if (!px.isNaN() && !py.isNaN()) {
            val depthFactor = ((pz + camZ * 0.4f) / (camZ * 0.8f)).coerceIn(0.4f, 1.5f)
            val pRadius = p.radius * scale * depthFactor

            val pColor = when (p.colorType) {
                0 -> Color(0xFF00F0FF) // Electric Cyan
                1 -> Color(0xFFA855F7) // Neon Violet
                2 -> Color(0xFFEC4899) // Hot Magenta
                else -> Color.White
            }

            // Core luminous spark
            drawCircle(
                color = Color.White.copy(alpha = (0.95f * depthFactor * intensity).coerceIn(0f, 1f)),
                radius = pRadius * 0.65f,
                center = Offset(px, py)
            )
            // Diffuse aura
            drawCircle(
                color = pColor.copy(alpha = (0.50f * depthFactor * intensity).coerceIn(0f, 0.85f)),
                radius = pRadius * 2.4f,
                center = Offset(px, py)
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 8. CENTRAL FLOATING HOLOGRAPHIC VALIDATION CORE (Optional / Hero)
    // ─────────────────────────────────────────────────────────────────────
    if (showCenterCore) {
        drawHolographicValidationCore(
            cx = cx,
            cy = cy,
            scale = scale,
            timeSec = timeSec,
            intensity = intensity,
            pageIndex = pageIndex,
            badgeIcon = badgeIcon,
            badgePath = state.badgePath,
            checkPath = state.checkPath,
            reduceMotion = reduceMotion
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // 9. FRONT-HALF ORBITAL RINGS & GLOWING PEARL NODES
    // ─────────────────────────────────────────────────────────────────────
    if (showOrbitalRings) {
        drawOrbitalTrajectories(
            cx = cx,
            cy = cy,
            majorR = majorR,
            scale = scale,
            intensity = intensity,
            drawBackHalf = false
        )
        drawOrbitalNodes(
            state = state,
            cx = cx,
            cy = cy,
            majorR = majorR,
            scale = scale,
            intensity = intensity,
            reduceMotion = reduceMotion
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENT RENDERERS
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawAmbientCyberpunkGlow(
    cx: Float,
    cy: Float,
    radius: Float,
    intensity: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFF6D28D9).copy(alpha = 0.32f * intensity),
                0.40f to Color(0xFF2563EB).copy(alpha = 0.14f * intensity),
                0.75f to Color(0xFF00F0FF).copy(alpha = 0.06f * intensity),
                1.0f to Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = radius
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawOrbitalTrajectories(
    cx: Float,
    cy: Float,
    majorR: Float,
    scale: Float,
    intensity: Float,
    drawBackHalf: Boolean
) {
    val orbits = listOf(
        Triple(1.38f * majorR, 0.68f * majorR, 24f),  // Ellipse 1 (tilted 24 deg)
        Triple(1.26f * majorR, 0.52f * majorR, -32f), // Ellipse 2 (tilted -32 deg)
        Triple(1.46f * majorR, 0.84f * majorR, 8f)    // Ellipse 3 (tilted 8 deg)
    )

    orbits.forEachIndexed { i, (rx, ry, tiltDeg) ->
        val tiltRad = tiltDeg * (PI.toFloat() / 180f)
        val cosT = cos(tiltRad); val sinT = sin(tiltRad)
        val steps = 72
        val path = Path()

        var started = false
        val halfStart = if (drawBackHalf) steps / 2 else 0
        val halfEnd = if (drawBackHalf) steps else steps / 2

        for (k in halfStart..halfEnd) {
            val theta = (k.toFloat() / steps.toFloat()) * (2f * PI.toFloat())
            val ex = rx * cos(theta)
            val ey = ry * sin(theta)

            val rotX = ex * cosT - ey * sinT
            val rotY = ex * sinT + ey * cosT

            val sx = cx + rotX
            val sy = cy + rotY

            if (!started) {
                path.moveTo(sx, sy)
                started = true
            } else {
                path.lineTo(sx, sy)
            }
        }

        val orbitAlpha = (if (drawBackHalf) 0.16f else 0.45f) * intensity
        val orbitColor = when (i) {
            0 -> Color(0xFF00F0FF) // Electric Cyan
            1 -> Color(0xFFA855F7) // Neon Violet
            else -> Color(0xFFEC4899) // Hot Magenta
        }

        drawPath(
            path = path,
            color = orbitColor.copy(alpha = orbitAlpha.coerceIn(0.04f, 0.90f)),
            style = Stroke(width = 1.0f * scale, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawOrbitalNodes(
    state: CyberpunkMobiusState,
    cx: Float,
    cy: Float,
    majorR: Float,
    scale: Float,
    intensity: Float,
    reduceMotion: Boolean
) {
    val orbits = listOf(
        Triple(1.38f * majorR, 0.68f * majorR, 24f),
        Triple(1.26f * majorR, 0.52f * majorR, -32f),
        Triple(1.46f * majorR, 0.84f * majorR, 8f)
    )

    for (node in state.orbitalNodes) {
        val (rx, ry, tiltDeg) = orbits[node.orbitIndex]
        val tiltRad = tiltDeg * (PI.toFloat() / 180f)
        val cosT = cos(tiltRad); val sinT = sin(tiltRad)

        if (!reduceMotion) {
            node.angleNorm = (node.angleNorm + node.speed * 0.016f) % 1.0f
        }
        val theta = node.angleNorm * 2f * PI.toFloat()

        val ex = rx * cos(theta)
        val ey = ry * sin(theta)
        val ez = sin(theta) // depth proxy

        val rotX = ex * cosT - ey * sinT
        val rotY = ex * sinT + ey * cosT

        val nx = cx + rotX
        val ny = cy + rotY

        // Draw nodes positioned in the front half (depth > 0)
        if (ez >= -0.2f) {
            val nodeRadius = node.size * scale
            // Inner pure white core
            drawCircle(
                color = Color.White.copy(alpha = (0.95f * intensity).coerceIn(0f, 1f)),
                radius = nodeRadius * 0.55f,
                center = Offset(nx, ny)
            )
            // Outer vibrant colored halo
            drawCircle(
                color = node.color.copy(alpha = (0.70f * intensity).coerceIn(0f, 0.9f)),
                radius = nodeRadius * 2.0f,
                center = Offset(nx, ny)
            )
        }
    }
}

private fun DrawScope.drawMultiPassNeonEdges(
    state: CyberpunkMobiusState,
    camZ: Float,
    scale: Float,
    timeSec: Float,
    intensity: Float,
    reduceMotion: Boolean
) {
    val edgeTIndices = listOf(0, T_STEPS)

    for (edgeTi in edgeTIndices) {
        state.edgePath.reset()
        var started = false

        for (ui in 0..U_STEPS) {
            val idx = ui * (T_STEPS + 1) + edgeTi
            val px = state.scrX[idx]; val py = state.scrY[idx]
            if (!px.isNaN() && !py.isNaN()) {
                if (!started) {
                    state.edgePath.moveTo(px, py)
                    started = true
                } else {
                    state.edgePath.lineTo(px, py)
                }
            }
        }

        // Pass 1: Wide soft diffuse neon bloom
        drawPath(
            path = state.edgePath,
            color = Color(0xFF00F0FF).copy(alpha = (0.24f * intensity).coerceIn(0.04f, 0.8f)),
            style = Stroke(width = 5.2f * scale, cap = StrokeCap.Round)
        )

        // Pass 2: Focused electric cyan/magenta stroke
        drawPath(
            path = state.edgePath,
            color = if (edgeTi == 0) Color(0xFF00F0FF).copy(alpha = (0.72f * intensity).coerceIn(0.1f, 0.95f))
                    else Color(0xFFEC4899).copy(alpha = (0.65f * intensity).coerceIn(0.1f, 0.95f)),
            style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round)
        )

        // Pass 3: Crisp laser-white filament core
        drawPath(
            path = state.edgePath,
            color = Color.White.copy(alpha = (0.94f * intensity).coerceIn(0.1f, 1f)),
            style = Stroke(width = 1.0f * scale, cap = StrokeCap.Round)
        )
    }

    // Edge traveling energy pulse knots
    val pulseCount = 6
    for (pi in 0 until pulseCount) {
        val pulseProgress = if (reduceMotion) (pi.toFloat() / pulseCount.toFloat())
            else (((timeSec * 0.16f) + (pi.toFloat() / pulseCount.toFloat())) % 1.0f)
        val ui = (pulseProgress * U_STEPS).toInt().coerceIn(0, U_STEPS)
        val edgeIdx = ui * (T_STEPS + 1) + 0 // on outer rim

        val px = state.scrX[edgeIdx]; val py = state.scrY[edgeIdx]; val pz = state.scrZ[edgeIdx]
        if (!px.isNaN()) {
            val depthFactor = ((pz + camZ * 0.4f) / (camZ * 0.8f)).coerceIn(0.5f, 1.5f)
            val pRadius = 3.8f * scale * depthFactor

            // Pure white spark
            drawCircle(
                color = Color.White.copy(alpha = intensity.coerceIn(0f, 1f)),
                radius = pRadius * 0.7f,
                center = Offset(px, py)
            )
            // Radiant cyan electric aura
            drawCircle(
                color = Color(0xFF00F0FF).copy(alpha = (0.80f * intensity).coerceIn(0f, 1f)),
                radius = pRadius * 2.4f,
                center = Offset(px, py)
            )
        }
    }
}

private fun DrawScope.drawHolographicValidationCore(
    cx: Float,
    cy: Float,
    scale: Float,
    timeSec: Float,
    intensity: Float,
    pageIndex: Int,
    badgeIcon: ImageVector?,
    badgePath: Path,
    checkPath: Path,
    reduceMotion: Boolean
) {
    val pulse = if (reduceMotion) 1f else (1.0f + 0.045f * sin(timeSec * 2.0f))
    val coreR = 48f * scale * pulse

    // 1. Ambient radial purple core aura
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFF8B5CF6).copy(alpha = 0.44f * intensity),
                0.5f to Color(0xFF6D28D9).copy(alpha = 0.22f * intensity),
                1.0f to Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = coreR * 1.95f
        ),
        radius = coreR * 1.95f,
        center = Offset(cx, cy)
    )

    // 2. Outer thin concentric techno ring
    drawCircle(
        color = Color(0xFF8B5CF6).copy(alpha = 0.48f * intensity),
        radius = coreR * 1.34f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.2f * scale)
    )

    // 3. Rotating segmented orbital energy ring
    val rotAngle = if (reduceMotion) 0.5f else (timeSec * 0.45f)
    val ringR = coreR * 1.16f
    val segmentCount = 16
    for (s in 0 until segmentCount) {
        val startAng = rotAngle + (s.toFloat() / segmentCount.toFloat()) * 2f * PI.toFloat()
        val p1x = cx + ringR * cos(startAng)
        val p1y = cy + ringR * sin(startAng)
        val p2x = cx + ringR * cos(startAng + 0.18f)
        val p2y = cy + ringR * sin(startAng + 0.18f)

        drawLine(
            color = if (s % 2 == 0) Color(0xFF00F0FF).copy(alpha = 0.78f * intensity)
                    else Color(0xFFA855F7).copy(alpha = 0.50f * intensity),
            start = Offset(p1x, p1y),
            end = Offset(p2x, p2y),
            strokeWidth = 1.4f * scale,
            cap = StrokeCap.Round
        )
    }

    // 4. Dark translucent obsidian/glass sphere disc
    drawCircle(
        color = Color(0xFF080614).copy(alpha = 0.88f * intensity),
        radius = coreR,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFF8B5CF6).copy(alpha = 0.65f * intensity),
        radius = coreR,
        center = Offset(cx, cy),
        style = Stroke(width = 1.6f * scale)
    )

    // 5. Scalloped Rosette Validation Badge / Icon
    val badgeR = coreR * 0.58f
    badgePath.reset()
    val petals = 12
    for (k in 0 until (petals * 2)) {
        val angle = (k.toFloat() / (petals * 2f)) * (2f * PI.toFloat()) - (PI.toFloat() * 0.5f)
        val r = if (k % 2 == 0) badgeR else badgeR * 0.82f
        val bx = cx + r * cos(angle)
        val by = cy + r * sin(angle)
        if (k == 0) badgePath.moveTo(bx, by) else badgePath.lineTo(bx, by)
    }
    badgePath.close()

    val badgeColor = when (pageIndex) {
        0 -> Color(0xFF00F0FF) // Requirements
        1 -> Color(0xFFEC4899) // Design AI
        else -> Color(0xFF8B5CF6) // Validate & Trace
    }

    drawPath(path = badgePath, color = badgeColor.copy(alpha = 0.88f * intensity))

    // 6. Crisp White Checkmark inside rosette
    checkPath.reset()
    val chkScale = badgeR * 0.44f
    checkPath.moveTo(cx - chkScale * 0.8f, cy - chkScale * 0.05f)
    checkPath.lineTo(cx - chkScale * 0.15f, cy + chkScale * 0.60f)
    checkPath.lineTo(cx + chkScale * 0.85f, cy - chkScale * 0.65f)

    drawPath(
        path = checkPath,
        color = Color.White.copy(alpha = intensity),
        style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}
