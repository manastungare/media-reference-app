package com.example.mediareferenceapp

import java.text.SimpleDateFormat
import java.util.*

// Use only Arabic numerals in the Latin script [0-9] in all dates. Use local timezones.
private val ISO_8601_TIME_ONLY = SimpleDateFormat("'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
  timeZone = TimeZone.getDefault()
}

fun Long.toIso8601Time(): String = ISO_8601_TIME_ONLY.format(Date(this))
