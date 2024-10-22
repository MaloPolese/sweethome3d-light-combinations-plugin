package com.lightscombinations.utils

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

fun mergeDateAndTimeToUTC(date: Date, time: Date): Calendar {
    val dateCalendar = GregorianCalendar().apply { this.time = date }
    val timeCalendar = GregorianCalendar().apply { this.time = time }

    return GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
        set(dateCalendar[Calendar.YEAR], dateCalendar[Calendar.MONTH], dateCalendar[Calendar.DAY_OF_MONTH],
            timeCalendar[Calendar.HOUR_OF_DAY], timeCalendar[Calendar.MINUTE], timeCalendar[Calendar.SECOND])
    }
}