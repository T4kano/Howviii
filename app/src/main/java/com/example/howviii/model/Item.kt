package com.example.howviii.model

import java.util.Date

data class Item(
    var uuid: String = "",
    var title: String = "",
    var description: String = "",
    var local: String = "",
    var contact: String = "",
    var imageUrl: String = "",
    var campusId: String = "",
    var createdBy: String = "",
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var status: String = "ativo",
    var type: String = "perdido"
)

