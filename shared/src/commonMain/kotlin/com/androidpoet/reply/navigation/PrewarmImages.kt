package com.androidpoet.reply.navigation

import androidx.compose.runtime.Composable
import com.androidpoet.reply.data.resources.Res
import com.androidpoet.reply.data.resources.paris_1
import com.androidpoet.reply.data.resources.paris_2
import com.androidpoet.reply.data.resources.paris_3
import com.androidpoet.reply.data.resources.paris_4
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun PrewarmImages() {
    painterResource(Res.drawable.paris_1)
    painterResource(Res.drawable.paris_2)
    painterResource(Res.drawable.paris_3)
    painterResource(Res.drawable.paris_4)
}
