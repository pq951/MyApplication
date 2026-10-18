package com.example.myapplication.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatMoney(value: Double): String = String.format(Locale.CHINA, "¥%.2f", value)

fun formatPrice(value: Double): String = String.format(Locale.CHINA, "%.2f", value)

fun formatTime(timestamp: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(timestamp))
