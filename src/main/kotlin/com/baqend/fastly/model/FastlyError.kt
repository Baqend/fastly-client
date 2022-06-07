package com.baqend.fastly.model

import com.google.gson.annotations.SerializedName

data class FastlyError(
        @SerializedName("msg")
        val message: String? = null,
        @SerializedName("detail")
        val detail: String? = null
)
