package com.baqend.fastly.model

import com.google.gson.annotations.SerializedName

data class Version(
    val number: Int,
    @SerializedName("service_id")
    val serviceId: String,
    val comment: String? = "",
    val testing: Boolean?,
    val staging: Boolean?,
    val deployed: Boolean?,
    val active: Boolean = false,
    val locked: Boolean? = false
) {
    fun isActive() = active
}
