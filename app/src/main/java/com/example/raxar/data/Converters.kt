package com.example.raxar.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class Converters {
    @TypeConverter fun zonedDateTimeToTimestamp(zonedDateTime: ZonedDateTime): Long = zonedDateTime.toInstant().toEpochMilli()

    @TypeConverter fun timestampToZonedDateTime(timestamp: Long): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
}