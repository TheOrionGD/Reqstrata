package com.theoriongd.reqstrata.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.theoriongd.reqstrata.domain.model.UmlDiagramType

@Composable
fun UmlDiagramCanvas(
    diagramType: UmlDiagramType,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .background(Color(0xFF18181B), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.6f, 3.0f)
                    offset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Background subtle grid
            val gridStep = 40f * scale
            val startX = (offset.x % gridStep)
            val startY = (offset.y % gridStep)
            var x = startX
            while (x < canvasWidth) {
                drawLine(
                    color = Color(0xFF27272A),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = startY
            while (y < canvasHeight) {
                drawLine(
                    color = Color(0xFF27272A),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Draw diagram based on type
            when (diagramType) {
                UmlDiagramType.USE_CASE -> drawUseCaseDiagram(scale, offset)
                UmlDiagramType.CLASS_DIAGRAM -> drawClassDiagram(scale, offset)
                UmlDiagramType.SEQUENCE -> drawSequenceDiagram(scale, offset)
                UmlDiagramType.ACTIVITY -> drawActivityDiagram(scale, offset)
                UmlDiagramType.COMPONENT -> drawComponentDiagram(scale, offset)
                UmlDiagramType.DEPLOYMENT -> drawDeploymentDiagram(scale, offset)
            }
        }

        // Overlay zoom indicator & reset
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0x991E293B),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${(scale * 100).toInt()}% • Pan & Pinch",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA1A1AA),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawUseCaseDiagram(scale: Float, offset: Offset) {
    val cx = size.width / 2 + offset.x
    val cy = size.height / 2 + offset.y

    // System Boundary
    drawRoundRect(
        color = Color(0xFF8B5CF6).copy(alpha = 0.3f),
        topLeft = Offset(cx - 140f * scale, cy - 220f * scale),
        size = Size(360f * scale, 440f * scale),
        cornerRadius = CornerRadius(16f * scale),
        style = Stroke(width = 2f * scale, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
    )

    // Actor 1 (User)
    val a1x = cx - 220f * scale
    val a1y = cy - 40f * scale
    drawCircle(Color(0xFF8B5CF6), radius = 18f * scale, center = Offset(a1x, a1y - 30f * scale), style = Stroke(3f * scale))
    drawLine(Color(0xFF8B5CF6), Offset(a1x, a1y - 12f * scale), Offset(a1x, a1y + 35f * scale), strokeWidth = 3f * scale)
    drawLine(Color(0xFF8B5CF6), Offset(a1x - 22f * scale, a1y + 6f * scale), Offset(a1x + 22f * scale, a1y + 6f * scale), strokeWidth = 3f * scale)
    drawLine(Color(0xFF8B5CF6), Offset(a1x, a1y + 35f * scale), Offset(a1x - 18f * scale, a1y + 70f * scale), strokeWidth = 3f * scale)
    drawLine(Color(0xFF8B5CF6), Offset(a1x, a1y + 35f * scale), Offset(a1x + 18f * scale, a1y + 70f * scale), strokeWidth = 3f * scale)

    // Ellipse Use Cases
    val ucs = listOf(
        Pair("Submit Requirement", Offset(cx + 40f * scale, cy - 140f * scale)),
        Pair("Analyze with Gemini", Offset(cx + 40f * scale, cy - 30f * scale)),
        Pair("Approve Artifacts", Offset(cx + 40f * scale, cy + 80f * scale)),
        Pair("Run Test Suite", Offset(cx + 40f * scale, cy + 180f * scale))
    )

    ucs.forEach { (label, center) ->
        drawOval(
            color = Color(0xFF27272A),
            topLeft = Offset(center.x - 110f * scale, center.y - 32f * scale),
            size = Size(220f * scale, 64f * scale)
        )
        drawOval(
            color = Color(0xFF8B5CF6),
            topLeft = Offset(center.x - 110f * scale, center.y - 32f * scale),
            size = Size(220f * scale, 64f * scale),
            style = Stroke(2f * scale)
        )
        // Connect to Actor
        drawLine(
            color = Color(0xFF71717A),
            start = Offset(a1x + 20f * scale, a1y + 10f * scale),
            end = Offset(center.x - 110f * scale, center.y),
            strokeWidth = 1.8f * scale
        )
    }
}

private fun DrawScope.drawClassDiagram(scale: Float, offset: Offset) {
    val cx = size.width / 2 + offset.x
    val cy = size.height / 2 + offset.y

    // Class 1: Requirement
    drawClassBox(
        name = "Requirement",
        attributes = listOf("+ id: UUID", "+ code: String", "+ priority: Priority", "+ status: Status"),
        methods = listOf("+ approve()", "+ analyzeQuality()", "+ createVersion()"),
        topLeft = Offset(cx - 240f * scale, cy - 180f * scale),
        scale = scale
    )

    // Class 2: UseCase
    drawClassBox(
        name = "UseCase",
        attributes = listOf("+ code: String", "+ actor: String", "+ goal: String"),
        methods = listOf("+ generateFlows()", "+ validate()"),
        topLeft = Offset(cx + 40f * scale, cy - 180f * scale),
        scale = scale
    )

    // Class 3: ArchitectureComponent
    drawClassBox(
        name = "ArchitectureComponent",
        attributes = listOf("+ code: String", "+ style: Style", "+ layer: String"),
        methods = listOf("+ addDependency()", "+ linkRequirement()"),
        topLeft = Offset(cx - 240f * scale, cy + 70f * scale),
        scale = scale
    )

    // Class 4: TestCase
    drawClassBox(
        name = "TestCase",
        attributes = listOf("+ code: String", "+ severity: Severity", "+ status: Status"),
        methods = listOf("+ execute(): Result", "+ recordEvidence()"),
        topLeft = Offset(cx + 40f * scale, cy + 70f * scale),
        scale = scale
    )

    // Association Arrow (Requirement -> UseCase)
    drawLine(
        color = Color(0xFF8B5CF6),
        start = Offset(cx - 40f * scale, cy - 100f * scale),
        end = Offset(cx + 40f * scale, cy - 100f * scale),
        strokeWidth = 2f * scale
    )
    // Draw diamond on Requirement side
    val path = Path().apply {
        moveTo(cx - 40f * scale, cy - 100f * scale)
        lineTo(cx - 50f * scale, cy - 106f * scale)
        lineTo(cx - 60f * scale, cy - 100f * scale)
        lineTo(cx - 50f * scale, cy - 94f * scale)
        close()
    }
    drawPath(path, color = Color(0xFF8B5CF6))

    // Requirement -> Architecture
    drawLine(
        color = Color(0xFF8B5CF6),
        start = Offset(cx - 140f * scale, cy - 20f * scale),
        end = Offset(cx - 140f * scale, cy + 70f * scale),
        strokeWidth = 2f * scale
    )

    // Requirement -> TestCase
    drawLine(
        color = Color(0xFF10B981),
        start = Offset(cx - 60f * scale, cy - 20f * scale),
        end = Offset(cx + 60f * scale, cy + 70f * scale),
        strokeWidth = 2f * scale
    )
}

private fun DrawScope.drawClassBox(
    name: String,
    attributes: List<String>,
    methods: List<String>,
    topLeft: Offset,
    scale: Float
) {
    val boxWidth = 200f * scale
    val headerHeight = 36f * scale
    val attrHeight = (attributes.size * 18f + 10f) * scale
    val methHeight = (methods.size * 18f + 10f) * scale
    val totalHeight = headerHeight + attrHeight + methHeight

    // Box body
    drawRoundRect(
        color = Color(0xFF27272A),
        topLeft = topLeft,
        size = Size(boxWidth, totalHeight),
        cornerRadius = CornerRadius(8f * scale)
    )
    drawRoundRect(
        color = Color(0xFF8B5CF6),
        topLeft = topLeft,
        size = Size(boxWidth, totalHeight),
        cornerRadius = CornerRadius(8f * scale),
        style = Stroke(2f * scale)
    )

    // Header divider
    drawLine(
        color = Color(0xFF8B5CF6),
        start = Offset(topLeft.x, topLeft.y + headerHeight),
        end = Offset(topLeft.x + boxWidth, topLeft.y + headerHeight),
        strokeWidth = 2f * scale
    )
    // Attributes divider
    drawLine(
        color = Color(0xFF3F3F46),
        start = Offset(topLeft.x, topLeft.y + headerHeight + attrHeight),
        end = Offset(topLeft.x + boxWidth, topLeft.y + headerHeight + attrHeight),
        strokeWidth = 1.5f * scale
    )
}

private fun DrawScope.drawSequenceDiagram(scale: Float, offset: Offset) {
    val cx = size.width / 2 + offset.x
    val cy = size.height / 2 + offset.y

    val actors = listOf("Client UI", "API Gateway", "Auth Service", "App Database")
    val colWidth = 140f * scale
    val startX = cx - (actors.size * colWidth) / 2 + 70f * scale
    val topY = cy - 200f * scale
    val bottomY = cy + 220f * scale

    actors.forEachIndexed { i, _ ->
        val x = startX + i * colWidth
        // Header Box
        drawRoundRect(
            color = Color(0xFF27272A),
            topLeft = Offset(x - 55f * scale, topY),
            size = Size(110f * scale, 38f * scale),
            cornerRadius = CornerRadius(6f * scale)
        )
        drawRoundRect(
            color = Color(0xFF8B5CF6),
            topLeft = Offset(x - 55f * scale, topY),
            size = Size(110f * scale, 38f * scale),
            cornerRadius = CornerRadius(6f * scale),
            style = Stroke(1.5f * scale)
        )

        // Lifeline dashed
        drawLine(
            color = Color(0xFF475569),
            start = Offset(x, topY + 38f * scale),
            end = Offset(x, bottomY),
            strokeWidth = 2f * scale,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
        )
    }

    // Message 1: UI -> Gateway
    val y1 = topY + 80f * scale
    drawArrow(Offset(startX, y1), Offset(startX + colWidth, y1), Color(0xFF8B5CF6), scale)

    // Message 2: Gateway -> Auth
    val y2 = topY + 130f * scale
    drawArrow(Offset(startX + colWidth, y2), Offset(startX + colWidth * 2, y2), Color(0xFF8B5CF6), scale)

    // Message 3: Auth -> DB
    val y3 = topY + 180f * scale
    drawArrow(Offset(startX + colWidth * 2, y3), Offset(startX + colWidth * 3, y3), Color(0xFF8B5CF6), scale)

    // Return 1: DB -> Auth (dashed)
    val y4 = topY + 240f * scale
    drawArrow(Offset(startX + colWidth * 3, y4), Offset(startX + colWidth * 2, y4), Color(0xFF10B981), scale, isDashed = true)

    // Return 2: Auth -> Gateway (dashed)
    val y5 = topY + 290f * scale
    drawArrow(Offset(startX + colWidth * 2, y5), Offset(startX + colWidth, y5), Color(0xFF10B981), scale, isDashed = true)

    // Return 3: Gateway -> UI
    val y6 = topY + 340f * scale
    drawArrow(Offset(startX + colWidth, y6), Offset(startX, y6), Color(0xFF10B981), scale, isDashed = true)
}

private fun DrawScope.drawActivityDiagram(scale: Float, offset: Offset) {
    val cx = size.width / 2 + offset.x
    val cy = size.height / 2 + offset.y

    // Start Node (solid circle)
    var curY = cy - 200f * scale
    drawCircle(Color(0xFF8B5CF6), radius = 16f * scale, center = Offset(cx, curY))

    // Arrow to Step 1
    drawArrow(Offset(cx, curY + 16f * scale), Offset(cx, curY + 50f * scale), Color(0xFF71717A), scale)
    curY += 50f * scale

    // Action 1: Parse Requirement
    drawActionBox("Parse Requirement Text", Offset(cx - 100f * scale, curY), 200f * scale, 44f * scale, scale)
    drawArrow(Offset(cx, curY + 44f * scale), Offset(cx, curY + 80f * scale), Color(0xFF71717A), scale)
    curY += 80f * scale

    // Decision Diamond: Ambiguity Check
    val diamondPath = Path().apply {
        moveTo(cx, curY)
        lineTo(cx + 36f * scale, curY + 30f * scale)
        lineTo(cx, curY + 60f * scale)
        lineTo(cx - 36f * scale, curY + 30f * scale)
        close()
    }
    drawPath(diamondPath, color = Color(0xFF27272A))
    drawPath(diamondPath, color = Color(0xFFF59E0B), style = Stroke(2f * scale))

    // No (Fix Ambiguity) branch to left
    drawArrow(Offset(cx - 36f * scale, curY + 30f * scale), Offset(cx - 120f * scale, curY + 30f * scale), Color(0xFFEF4444), scale)
    drawActionBox("Clarify Input", Offset(cx - 240f * scale, curY + 10f * scale), 110f * scale, 40f * scale, scale)

    // Yes (Proceed) branch down
    drawArrow(Offset(cx, curY + 60f * scale), Offset(cx, curY + 100f * scale), Color(0xFF10B981), scale)
    curY += 100f * scale

    // Action 2: Generate Artifacts
    drawActionBox("Generate System Design & Tests", Offset(cx - 130f * scale, curY), 260f * scale, 44f * scale, scale)
    drawArrow(Offset(cx, curY + 44f * scale), Offset(cx, curY + 80f * scale), Color(0xFF71717A), scale)
    curY += 80f * scale

    // Final Node (Bullseye)
    drawCircle(Color(0xFF8B5CF6), radius = 18f * scale, center = Offset(cx, curY), style = Stroke(2.5f * scale))
    drawCircle(Color(0xFF8B5CF6), radius = 11f * scale, center = Offset(cx, curY))
}

private fun DrawScope.drawComponentDiagram(scale: Float, offset: Offset) {
    val cx = size.width / 2 + offset.x
    val cy = size.height / 2 + offset.y

    val comps = listOf(
        Pair("Web / Mobile Ingress", Offset(cx - 180f * scale, cy - 140f * scale)),
        Pair("Core Engine Service", Offset(cx + 40f * scale, cy - 140f * scale)),
        Pair("Gemini AI Copilot", Offset(cx - 180f * scale, cy + 60f * scale)),
        Pair("Relational Storage", Offset(cx + 40f * scale, cy + 60f * scale))
    )

    comps.forEach { (name, pos) ->
        val w = 170f * scale
        val h = 70f * scale
        drawRoundRect(Color(0xFF27272A), pos, Size(w, h), CornerRadius(8f * scale))
        drawRoundRect(Color(0xFF8B5CF6), pos, Size(w, h), CornerRadius(8f * scale), Stroke(2f * scale))

        // Component tabs on left side
        drawRect(Color(0xFF8B5CF6), Offset(pos.x - 12f * scale, pos.y + 14f * scale), Size(24f * scale, 12f * scale))
        drawRect(Color(0xFF8B5CF6), Offset(pos.x - 12f * scale, pos.y + 38f * scale), Size(24f * scale, 12f * scale))
    }

    // Connections between components
    drawArrow(Offset(cx - 10f * scale, cy - 105f * scale), Offset(cx + 40f * scale, cy - 105f * scale), Color(0xFF8B5CF6), scale)
    drawArrow(Offset(cx + 125f * scale, cy - 70f * scale), Offset(cx + 125f * scale, cy + 60f * scale), Color(0xFF8B5CF6), scale)
    drawArrow(Offset(cx - 95f * scale, cy - 70f * scale), Offset(cx - 95f * scale, cy + 60f * scale), Color(0xFFA78BFA), scale)
}

private fun DrawScope.drawDeploymentDiagram(scale: Float, offset: Offset) {
    val cx = size.width / 2 + offset.x
    val cy = size.height / 2 + offset.y

    // Node 1: Client Node
    drawNode3D("Client Device Node (Android)", Offset(cx - 200f * scale, cy - 160f * scale), 160f * scale, 110f * scale, scale)

    // Node 2: Cloud Ingress
    drawNode3D("Kubernetes App Cluster", Offset(cx + 40f * scale, cy - 160f * scale), 180f * scale, 120f * scale, scale)

    // Node 3: AI Inference Node
    drawNode3D("Gemini Model Endpoint", Offset(cx - 200f * scale, cy + 40f * scale), 160f * scale, 100f * scale, scale)

    // Node 4: Database Replica
    drawNode3D("Cloud SQL HA Replica", Offset(cx + 40f * scale, cy + 40f * scale), 180f * scale, 110f * scale, scale)

    // Network bus line
    drawLine(
        color = Color(0xFF8B5CF6),
        start = Offset(cx - 40f * scale, cy - 105f * scale),
        end = Offset(cx + 40f * scale, cy - 105f * scale),
        strokeWidth = 3f * scale
    )
    drawLine(
        color = Color(0xFF8B5CF6),
        start = Offset(cx + 130f * scale, cy - 40f * scale),
        end = Offset(cx + 130f * scale, cy + 40f * scale),
        strokeWidth = 3f * scale
    )
}

private fun DrawScope.drawActionBox(label: String, topLeft: Offset, width: Float, height: Float, scale: Float) {
    drawRoundRect(
        color = Color(0xFF27272A),
        topLeft = topLeft,
        size = Size(width, height),
        cornerRadius = CornerRadius(20f * scale)
    )
    drawRoundRect(
        color = Color(0xFF8B5CF6),
        topLeft = topLeft,
        size = Size(width, height),
        cornerRadius = CornerRadius(20f * scale),
        style = Stroke(2f * scale)
    )
}

private fun DrawScope.drawNode3D(name: String, topLeft: Offset, width: Float, height: Float, scale: Float) {
    val depth = 16f * scale
    // Front face
    drawRoundRect(
        color = Color(0xFF27272A),
        topLeft = Offset(topLeft.x, topLeft.y + depth),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f * scale)
    )
    drawRoundRect(
        color = Color(0xFF8B5CF6),
        topLeft = Offset(topLeft.x, topLeft.y + depth),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f * scale),
        style = Stroke(2f * scale)
    )

    // Top face
    val topFace = Path().apply {
        moveTo(topLeft.x, topLeft.y + depth)
        lineTo(topLeft.x + depth, topLeft.y)
        lineTo(topLeft.x + width + depth, topLeft.y)
        lineTo(topLeft.x + width, topLeft.y + depth)
        close()
    }
    drawPath(topFace, color = Color(0xFF18181B))
    drawPath(topFace, color = Color(0xFF8B5CF6), style = Stroke(1.5f * scale))

    // Right face
    val rightFace = Path().apply {
        moveTo(topLeft.x + width, topLeft.y + depth)
        lineTo(topLeft.x + width + depth, topLeft.y)
        lineTo(topLeft.x + width + depth, topLeft.y + height)
        lineTo(topLeft.x + width, topLeft.y + depth + height)
        close()
    }
    drawPath(rightFace, color = Color(0xFF18181B))
    drawPath(rightFace, color = Color(0xFF8B5CF6), style = Stroke(1.5f * scale))
}

private fun DrawScope.drawArrow(start: Offset, end: Offset, color: Color, scale: Float, isDashed: Boolean = false) {
    val effect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 2f * scale,
        pathEffect = effect
    )

    // Arrowhead
    val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
    val arrowLen = 12f * scale
    val arrowAngle = Math.PI / 6

    val x1 = end.x - arrowLen * Math.cos(angle - arrowAngle).toFloat()
    val y1 = end.y - arrowLen * Math.sin(angle - arrowAngle).toFloat()
    val x2 = end.x - arrowLen * Math.cos(angle + arrowAngle).toFloat()
    val y2 = end.y - arrowLen * Math.sin(angle + arrowAngle).toFloat()

    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(x1, y1)
        lineTo(x2, y2)
        close()
    }
    drawPath(path, color = color)
}
