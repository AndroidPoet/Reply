package com.androidpoet.reply.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidpoet.reply.data.resources.Res
import com.androidpoet.reply.data.resources.paris_1
import com.androidpoet.reply.data.resources.paris_2
import com.androidpoet.reply.data.resources.paris_3
import com.androidpoet.reply.data.resources.paris_4
import org.jetbrains.compose.resources.painterResource

/**
 * Decodes the large photos once at startup (they are cached by Compose Resources afterwards), so
 * the first card → detail container transform is not stalled by image decoding on its first frame.
 */
@Composable
fun PrewarmImages() {
    Box(Modifier.size(0.dp)) {
        painterResource(Res.drawable.paris_1)
        painterResource(Res.drawable.paris_2)
        painterResource(Res.drawable.paris_3)
        painterResource(Res.drawable.paris_4)
    }
}
