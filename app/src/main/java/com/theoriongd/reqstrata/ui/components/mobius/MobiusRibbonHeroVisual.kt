package com.theoriongd.reqstrata.ui.components.mobius

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.theoriongd.reqstrata.ui.motion.LocalReduceMotion

/**
 * Production 3D Futuristic Cyberpunk Möbius Ribbon Hero Visual.
 * Renders the central 3D ribbon with cybernetic grid, embedded micro-nodes,
 * luminous edge bloom, continuous lifecycle particles, orbital rings,
 * and the floating holographic validation core.
 */
@Composable
fun MobiusRibbonHeroVisual(
    modifier: Modifier = Modifier,
    pageIndex: Int = 2,
    badgeIcon: ImageVector? = null
) {
    val reduceMotion = LocalReduceMotion.current
    val mobiusState = rememberCyberpunkMobiusState()

    var timeNanos by remember { mutableLongStateOf(0L) }
    var startNanos by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            while (true) {
                withFrameNanos { frameNanos ->
                    if (startNanos < 0L) startNanos = frameNanos
                    timeNanos = frameNanos - startNanos
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        val timeSec = if (reduceMotion) 1.2f else (timeNanos / 1_000_000_000f)
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.5f
        val minDim = minOf(w, h)

        // Scale factor normalized to 340dp bounding box
        val scale = (minDim / 340f).coerceIn(0.65f, 1.45f)

        drawCyberpunkMobiusRibbon(
            state = mobiusState,
            cx = cx,
            cy = cy,
            scale = scale,
            timeSec = timeSec,
            intensity = 1.0f,
            showOrbitalRings = true,
            showCenterCore = true,
            pageIndex = pageIndex,
            badgeIcon = badgeIcon,
            reduceMotion = reduceMotion
        )
    }
}
