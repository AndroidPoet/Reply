package com.androidpoet.reply.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.designsystem.theme.ReplyDimens
import com.androidpoet.reply.designsystem.theme.ReplyTheme
import com.androidpoet.reply.designsystem.theme.elevated
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * `MaterialCardView` in Reply's `MediumComponent` shape (0dp corners), `colorSurface`, with an
 * independently animatable top-left corner (used to signal a starred email at 24dp).
 */
@Composable
fun ReplyCard(
    modifier: Modifier = Modifier,
    topLeftCorner: Dp = 0.dp,
    elevation: Dp = ReplyDimens.plane00,
    color: Color = ReplyTheme.colors.surface,
    content: @Composable () -> Unit,
) {
    val shape: Shape = RoundedCornerShape(topStart = topLeftCorner)
    val colors = ReplyTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, clip = false) else Modifier)
            .clip(shape)
            .background(colors.elevated(color, elevation)),
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.onSurfaceHigh) {
            content()
        }
    }
}

/** Circular-cropped avatar (`glideCircularCrop`). */
@Composable
fun Avatar(
    image: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = ReplyDimens.emailSenderProfileImageSize,
) {
    Image(
        painter = painterResource(image),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape),
    )
}

/** 1dp `@drawable/divider` tinted `color_on_surface_divider`. */
@Composable
fun ReplyDivider(
    modifier: Modifier = Modifier,
    color: Color = ReplyTheme.colors.onSurfaceDivider,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}

/** Text with an explicit `TextAppearance` style + emphasis colour; the everyday Reply label. */
@Composable
fun ReplyText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier,
    )
}
