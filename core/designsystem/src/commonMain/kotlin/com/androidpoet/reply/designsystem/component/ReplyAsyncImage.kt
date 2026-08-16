package com.androidpoet.reply.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.androidpoet.reply.data.ReplyImage
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.request.error

@Composable
fun ReplyAsyncImage(
    image: ReplyImage,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        request = ComposableImageRequest(image.uri) {
            crossfade()
            image.fallback?.let { error(it) }
        },
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}
