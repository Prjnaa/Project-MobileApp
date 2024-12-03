package com.project.projectmap.module

import java.text.SimpleDateFormat
import java.util.Date

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    return dateFormat.format(Date())
}