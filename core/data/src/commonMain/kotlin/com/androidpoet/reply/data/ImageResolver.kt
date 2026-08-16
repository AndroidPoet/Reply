package com.androidpoet.reply.data

import com.androidpoet.reply.data.resources.Res
import com.androidpoet.reply.data.resources.allDrawableResources
import com.github.panpf.sketch.fetch.newComposeResourceUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val IMAGE_BASE_URL =
    "https://raw.githubusercontent.com/AndroidPoet/Reply/main/core/data/src/commonMain/composeResources/drawable"

enum class DataSource { NONE, BUNDLED, REMOTE }

@Inject
@SingleIn(AppScope::class)
class ImageResolver {
    private val _source = MutableStateFlow(DataSource.NONE)
    val source: StateFlow<DataSource> = _source.asStateFlow()

    fun setSource(source: DataSource) {
        _source.value = source
    }

    @OptIn(ExperimentalResourceApi::class)
    fun image(fileName: String): ReplyImage = ReplyImage(
        uri = if (_source.value == DataSource.REMOTE) {
            "$IMAGE_BASE_URL/$fileName"
        } else {
            newComposeResourceUri(Res.getUri("drawable/$fileName"))
        },
        fallback = Res.allDrawableResources[fileName.substringBeforeLast('.')],
    )
}
