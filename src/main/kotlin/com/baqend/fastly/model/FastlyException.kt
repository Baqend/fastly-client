package com.baqend.fastly.model

class FastlyException
@JvmOverloads
constructor(
    override val message: String? = "",
    val status: Int? = null,
    val detail: String? = null,
    override val cause: Throwable? = null
) : RuntimeException("$message: $detail", cause) {

    constructor(cause: Throwable?) : this(message = cause?.message, cause = cause)

    override fun toString(): String {
        return super.toString() + (this.detail ?: ("\nDetail: " + this.detail))
    }
}
