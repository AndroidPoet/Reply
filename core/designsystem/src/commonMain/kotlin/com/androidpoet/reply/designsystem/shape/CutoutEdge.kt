package com.androidpoet.reply.designsystem.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.atan
import kotlin.math.sqrt

private const val ARC_QUARTER = 90f
private const val ARC_HALF = 180f
private const val ANGLE_UP = 270f
private const val ANGLE_LEFT = 180f

fun Path.addCutoutTopEdge(
    length: Float,
    cutoutMargin: Float,
    cutoutRoundedCornerRadius: Float,
    cutoutVerticalOffset: Float,
    cutoutDiameter: Float,
    cutoutHorizontalOffset: Float = 0f,
    interpolation: Float = 1f,

    startX: Float = 0f,
) {
    if (cutoutDiameter == 0f) {
        lineTo(startX + length, 0f)
        return
    }
    val cradleDiameter = cutoutMargin * 2 + cutoutDiameter
    val cradleRadius = cradleDiameter / 2f
    val roundedCornerOffset = interpolation * cutoutRoundedCornerRadius
    val middle = startX + length / 2f + cutoutHorizontalOffset

    val verticalOffset = interpolation * cutoutVerticalOffset + (1 - interpolation) * cradleRadius
    val verticalOffsetRatio = verticalOffset / cradleRadius
    if (verticalOffsetRatio >= 1.0f) {
        lineTo(startX + length, 0f)
        return
    }

    val distanceBetweenCenters = cradleRadius + roundedCornerOffset
    val distanceBetweenCentersSquared = distanceBetweenCenters * distanceBetweenCenters
    val distanceY = verticalOffset + roundedCornerOffset
    val distanceX = sqrt(distanceBetweenCentersSquared - distanceY * distanceY)

    val leftRoundedCornerCircleX = middle - distanceX
    val rightRoundedCornerCircleX = middle + distanceX

    val cornerRadiusArcLength = toDegrees(atan(distanceX / distanceY))
    val cutoutArcOffset = ARC_QUARTER - cornerRadiusArcLength

    lineTo(leftRoundedCornerCircleX - roundedCornerOffset, 0f)

    arcTo(
        rect = Rect(
            left = leftRoundedCornerCircleX - roundedCornerOffset,
            top = 0f,
            right = leftRoundedCornerCircleX + roundedCornerOffset,
            bottom = roundedCornerOffset * 2,
        ),
        startAngleDegrees = ANGLE_UP,
        sweepAngleDegrees = cornerRadiusArcLength,
        forceMoveTo = false,
    )

    arcTo(
        rect = Rect(
            left = middle - cradleRadius,
            top = -cradleRadius - verticalOffset,
            right = middle + cradleRadius,
            bottom = cradleRadius - verticalOffset,
        ),
        startAngleDegrees = ANGLE_LEFT - cutoutArcOffset,
        sweepAngleDegrees = cutoutArcOffset * 2 - ARC_HALF,
        forceMoveTo = false,
    )

    arcTo(
        rect = Rect(
            left = rightRoundedCornerCircleX - roundedCornerOffset,
            top = 0f,
            right = rightRoundedCornerCircleX + roundedCornerOffset,
            bottom = roundedCornerOffset * 2,
        ),
        startAngleDegrees = ANGLE_UP - cornerRadiusArcLength,
        sweepAngleDegrees = cornerRadiusArcLength,
        forceMoveTo = false,
    )
    lineTo(startX + length, 0f)
}

private fun toDegrees(radians: Float): Float = (radians * 180.0 / kotlin.math.PI).toFloat()

class CutoutTopEdgeShape(
    private val cutoutMargin: Dp,
    private val cutoutRoundedCornerRadius: Dp,
    private val cutoutVerticalOffset: Dp,
    private val cutoutDiameter: Dp,
    private val topCornerRadius: Dp,
    private val interpolation: Float = 1f,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        with(density) {
            val corner = topCornerRadius.toPx() * interpolation
            path.moveTo(0f, corner)
            if (corner > 0f) {
                path.arcTo(Rect(0f, 0f, corner * 2, corner * 2), 180f, 90f, false)
            }

            path.addCutoutTopEdge(
                length = size.width - corner * 2,
                cutoutMargin = cutoutMargin.toPx(),
                cutoutRoundedCornerRadius = cutoutRoundedCornerRadius.toPx(),
                cutoutVerticalOffset = cutoutVerticalOffset.toPx(),
                cutoutDiameter = cutoutDiameter.toPx(),
                interpolation = interpolation,
                startX = corner,
            )
            if (corner > 0f) {
                path.arcTo(Rect(size.width - corner * 2, 0f, size.width, corner * 2), 270f, 90f, false)
            }
            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()
        }
        return Outline.Generic(path)
    }
}
