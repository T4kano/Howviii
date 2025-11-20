package com.example.howviii.model

import java.util.Date

data class Campus(
    var uuid: String = "",
    var name: String = "",
    var description: String = "",
    var address: String = "",
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var active: Boolean = true
)
