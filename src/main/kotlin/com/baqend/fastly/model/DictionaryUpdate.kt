package com.baqend.fastly.model

import com.google.gson.annotations.SerializedName
import java.util.*

sealed class DictionaryItemUpdate
data class CreateDictionaryItem(

    @SerializedName("item_key")
    var itemKey: String,
    @SerializedName("item_value")
    var itemValue: String
) : DictionaryItemUpdate() {
    val op: String = "create"
}

data class UpdateDictionaryItem(

    @SerializedName("item_key")
    var itemKey: String,
    @SerializedName("item_value")
    var itemValue: String
) : DictionaryItemUpdate() {
    val op: String = "update"
}

data class UpsertDictionaryItem(
    @SerializedName("item_key")
    var itemKey: String,
    @SerializedName("item_value")
    var itemValue: String
) : DictionaryItemUpdate() {
    val op: String = "upsert"
}

data class DeleteDictionaryItem(
    @SerializedName("item_key")
    var itemKey: String
) : DictionaryItemUpdate() {
    val op: String = "delete"
}

class DictionaryUpdate(
    @SerializedName("items")
    var items: LinkedList<DictionaryItemUpdate> = LinkedList<DictionaryItemUpdate>()
) {
    fun create(key: String, value: String) = apply {
        this.items.add(CreateDictionaryItem(key, value))
    }

    fun update(key: String, value: String) = apply {
        this.items.add(UpdateDictionaryItem(key, value))
    }

    fun upsert(key: String, value: String) = apply {
        this.items.add(UpsertDictionaryItem(key, value))
    }

    fun delete(key: String) = apply {
        this.items.add(DeleteDictionaryItem(key))
    }
}
