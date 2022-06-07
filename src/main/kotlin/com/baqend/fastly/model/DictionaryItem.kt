package com.baqend.fastly.model

import com.google.gson.annotations.SerializedName

data class DictionaryItem(
    @SerializedName("dictionary_id")
    val dictionaryId: String,
    @SerializedName("service_id")
    val serviceId: String,
    @SerializedName("item_key")
    val key: String,
    @SerializedName("item_value")
    val value: String
)
