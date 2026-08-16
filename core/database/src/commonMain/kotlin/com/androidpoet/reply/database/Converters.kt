package com.androidpoet.reply.database

import androidx.room.TypeConverter

private const val RECORD = "\n"
private const val FIELD = "\t"

class Converters {
    @TypeConverter
    fun longListToString(value: List<Long>): String = value.joinToString(",")

    @TypeConverter
    fun stringToLongList(value: String): List<Long> =
        if (value.isEmpty()) emptyList() else value.split(',').map { it.toLong() }

    @TypeConverter
    fun attachmentsToString(value: List<AttachmentEmbedded>): String =
        value.joinToString(RECORD) { "${it.image}$FIELD${it.contentDesc}" }

    @TypeConverter
    fun stringToAttachments(value: String): List<AttachmentEmbedded> =
        if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(RECORD).map {
                val (image, desc) = it.split(FIELD, limit = 2)
                AttachmentEmbedded(image, desc)
            }
        }
}
