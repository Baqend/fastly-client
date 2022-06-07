package com.baqend.fastly.model

import com.google.gson.annotations.SerializedName

data class Dictionary(
    val id: String,
    val name: String,
    @SerializedName("service_id")
    val serviceId: String,
    val version: Int
)
