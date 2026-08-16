package com.androidpoet.reply.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.work_sans_bold
import com.androidpoet.reply.designsystem.resources.work_sans_medium
import com.androidpoet.reply.designsystem.resources.work_sans_regular
import com.androidpoet.reply.designsystem.resources.work_sans_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun workSansFamily(): FontFamily = FontFamily(
    Font(Res.font.work_sans_regular, FontWeight.Normal),
    Font(Res.font.work_sans_medium, FontWeight.Medium),
    Font(Res.font.work_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.work_sans_bold, FontWeight.Bold),
)

@Immutable
data class ReplyTypography(
    val headline2: TextStyle,
    val headline3: TextStyle,
    val headline4: TextStyle,
    val headline5: TextStyle,
    val headline6: TextStyle,
    val subtitle1: TextStyle,
    val subtitle2: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val button: TextStyle,
    val caption: TextStyle,
    val overline: TextStyle,
)

fun replyTypography(family: FontFamily): ReplyTypography = ReplyTypography(
    headline2 = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 60.sp, letterSpacing = (-0.5).sp),
    headline3 = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 48.sp, letterSpacing = 0.sp),
    headline4 = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = 0.25.sp),
    headline5 = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = 0.sp),
    headline6 = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 20.sp, letterSpacing = 0.15.sp),
    subtitle1 = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 16.sp, letterSpacing = 0.15.sp),
    subtitle2 = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.1.sp),
    body1 = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 16.sp, letterSpacing = 0.5.sp, lineHeight = 24.sp),
    body2 = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 0.25.sp),
    button = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 1.25.sp),
    caption = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.4.sp),
    overline = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.5.sp),
)

val LocalReplyTypography = staticCompositionLocalOf { replyTypography(FontFamily.SansSerif) }
