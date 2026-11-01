package com.bookmedia.gallery.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Clean, antislop geometric vector icons for social media platforms.
 * Adheres to high-contrast, monochrome minimalism with zero rainbow gradients.
 */
@Composable
fun PlatformIcon(
    platform: String,
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = Color.Unspecified
) {
    val norm = platform.trim().lowercase()
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = if (tint != Color.Unspecified) tint else Color.Black

        when (norm) {
            "x", "twitter" -> {
                // Modern clean X glyph
                val strokeW = w * 0.16f
                drawLine(
                    color = color,
                    start = Offset(w * 0.15f, h * 0.15f),
                    end = Offset(w * 0.85f, h * 0.85f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.85f, h * 0.15f),
                    end = Offset(w * 0.15f, h * 0.85f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
            "tiktok" -> {
                // Minimalist musical note glyph
                val path = Path().apply {
                    moveTo(w * 0.52f, h * 0.82f)
                    cubicTo(w * 0.35f, h * 0.82f, w * 0.28f, h * 0.72f, w * 0.28f, h * 0.60f)
                    cubicTo(w * 0.28f, h * 0.48f, w * 0.38f, h * 0.40f, w * 0.52f, h * 0.40f)
                    lineTo(w * 0.52f, h * 0.15f)
                    cubicTo(w * 0.58f, h * 0.28f, w * 0.70f, h * 0.35f, w * 0.85f, h * 0.36f)
                    lineTo(w * 0.85f, h * 0.23f)
                    cubicTo(w * 0.73f, h * 0.22f, w * 0.65f, h * 0.14f, w * 0.65f, h * 0.12f)
                    lineTo(w * 0.52f, h * 0.12f)
                    close()
                }
                drawPath(path = path, color = color)
            }
            "instagram" -> {
                // Minimalist camera outline + lens dot
                val strokeW = w * 0.11f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.12f, h * 0.12f),
                    size = Size(w * 0.76f, h * 0.76f),
                    cornerRadius = CornerRadius(w * 0.22f, h * 0.22f),
                    style = Stroke(width = strokeW)
                )
                drawCircle(
                    color = color,
                    radius = w * 0.20f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = strokeW)
                )
                drawCircle(
                    color = color,
                    radius = w * 0.05f,
                    center = Offset(w * 0.70f, h * 0.30f)
                )
            }
            "youtube" -> {
                // Rounded play pill
                val strokeW = w * 0.10f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.10f, h * 0.20f),
                    size = Size(w * 0.80f, h * 0.60f),
                    cornerRadius = CornerRadius(w * 0.18f, h * 0.18f),
                    style = Stroke(width = strokeW)
                )
                val playPath = Path().apply {
                    moveTo(w * 0.42f, h * 0.37f)
                    lineTo(w * 0.64f, h * 0.50f)
                    lineTo(w * 0.42f, h * 0.63f)
                    close()
                }
                drawPath(path = playPath, color = color)
            }
            "douyin" -> {
                // Distinct double-ring acoustic glyph
                drawCircle(
                    color = color,
                    radius = w * 0.36f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = w * 0.12f)
                )
                drawCircle(
                    color = color,
                    radius = w * 0.15f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
            else -> {
                // Fallback elegant hashtag / globe
                val strokeW = w * 0.12f
                drawLine(
                    color = color,
                    start = Offset(w * 0.35f, h * 0.15f),
                    end = Offset(w * 0.35f, h * 0.85f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.65f, h * 0.15f),
                    end = Offset(w * 0.65f, h * 0.85f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.15f, h * 0.38f),
                    end = Offset(w * 0.85f, h * 0.38f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.15f, h * 0.62f),
                    end = Offset(w * 0.85f, h * 0.62f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
