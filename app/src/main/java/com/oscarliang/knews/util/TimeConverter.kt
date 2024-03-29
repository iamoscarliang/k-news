package com.oscarliang.knews.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeConverter {

    fun getTimePassBy(hour: Int): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = System.currentTimeMillis()
        val hourInMillis = hour * 60 * 60 * 1000
        val diff = now - hourInMillis
        return dateFormat.format(Date(diff))
    }

}