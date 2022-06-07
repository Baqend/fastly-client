package com.baqend.fastly.model

import com.google.gson.annotations.SerializedName

data class Service(
    val id: String,
    val name: String,
    val versions: List<Version>,
    @SerializedName("customer_id")
    val customerId: String,
    val comment: String
)
