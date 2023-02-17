package com.baqend.fastly

import com.baqend.fastly.model.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URI
import kotlin.math.pow

/**
 * Convenience client to make request to the FastlyApi. It provides common tasks.
 */
class FastlyClient(apiUrl: URI, apiKeys: Array<String>) {
    private var httpClient: HttpClient

    init {
        this.httpClient = HttpClient(OkHttp) {
            engine {
                config {
                    retryOnConnectionFailure(false)
                }

                addInterceptor(
                    Interceptor {
                        val req = it.request()
                        var retryCount = 0
                        while (retryCount < 2) {
                            val response: Response = it.proceed(req)

                            if (!(response.code == 409 || response.code >= 500)) {
                                return@Interceptor response
                            } else {
                                retryCount++
                                response.close()
                                val backOffDelay = 2.0.pow(retryCount.toDouble()).toLong() * 1000L
                                runBlocking { delay(backOffDelay) }
                            }
                        }

                        return@Interceptor it.proceed(req)
                    }
                )
            }
            install(ContentNegotiation) {
                val converter = GsonConverter(
                    GsonBuilder()
                        .serializeNulls()
                        .create()
                )
                register(ContentType.Application.Json, converter)
                register(ContentType("text", "json"), converter)
            }

// FIXME: https://github.com/ktorio/ktor/issues/1038
//            HttpResponseValidator {
//                validateResponse {
//                    val status = it.status.value
//                    if (status >= 400) {
//                        val error = it.receive<FastlyError>()
//                        throw FastlyException(message = error.message, status = status, detail = error.detail)
//
//                    }
//                }
//            }
            expectSuccess = false

            defaultRequest {
                host = apiUrl.host
                if (apiUrl.port != -1) port = apiUrl.port
                url { protocol = URLProtocol.createOrDefault(apiUrl.toURL().protocol) }
                header("Fastly-Key", apiKeys.first())
            }
        }
    }

    /**
     *Get the active version number from a service
     * @param service the Service
     * @return the number of the active service
     */
    fun getActiveServiceVersionNumber(service: Service) = service?.versions?.filter { it.isActive() }?.first()?.number
        ?: throw BadRequestException("No active version found in this service")

//region Dictionary Items
    /**
     * Retrieve a single [DictionaryItem] given service, dictionary ID and item key
     * @param serviceId The service id
     * @param dictionaryId The dictionary id
     * @param key The item key
     * @return The [DictionaryItem] or null if the entry was not found
     */
    fun getDictionaryItem(serviceId: String, dictionaryId: String, key: String) = runBlocking(Dispatchers.IO) {
        return@runBlocking httpClient.get {
            url {
                encodedPath = "/service/$serviceId/dictionary/$dictionaryId/item/$key"
            }
        }.handleNullable<DictionaryItem?>()
    }

    /**
     * List of [DictionaryItem] given service and dictionary ID.
     * @param serviceId The service id
     * @param dictionaryId The dictionary id
     * @return All items of the dictionary or null if no items were found
     */
    fun getDictionaryItems(serviceId: String, dictionaryId: String) = runBlocking(Dispatchers.IO) {
        return@runBlocking httpClient.get {
            url {
                encodedPath = "/service/$serviceId/dictionary/$dictionaryId/items"
            }
        }.handleNullable<List<DictionaryItem>?>()
    }

    /**
     * Upsert [DictionaryItem] given service, dictionary ID, item key, and item value.
     * @param serviceId The service id
     * @param dictionaryId The dictionary id
     * @param key The item key
     * @param value The item value
     * @return The updated [DictionaryItem]
     */
    fun setDictionaryItem(serviceId: String, dictionaryId: String, key: String, value: String) =
        runBlocking(Dispatchers.IO) {
            return@runBlocking httpClient.put {
                url {
                    encodedPath = "/service/$serviceId/dictionary/$dictionaryId/item/$key"
                }
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("item_key", key)
                            append("item_value", value)
                        }
                    )
                )
            }.handle<DictionaryItem>()
        }

    /**
     * Create [DictionaryItem] given service, dictionary ID, item key, and item value.
     * @param serviceId The service id
     * @param dictionaryId The dictionary id
     * @param key The item key
     * @param value The item value
     * @return The updated [DictionaryItem]
     */
    fun createDictionaryItem(serviceId: String, dictionaryId: String, key: String, value: String) =
        runBlocking(Dispatchers.IO) {
            return@runBlocking httpClient.post {
                url {
                    encodedPath = "/service/$serviceId/dictionary/$dictionaryId/item"
                }
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("item_key", key)
                            append("item_value", value)
                        }
                    )
                )
            }.handle<DictionaryItem>()
        }

    /**
     * Update [DictionaryItem] given service, dictionary ID, item key, and item value.
     * @param serviceId The service id
     * @param dictionaryId The dictionary id
     * @param key The item key
     * @param value The item value
     * @return The updated [DictionaryItem]
     */
    fun updateDictionaryItem(serviceId: String, dictionaryId: String, key: String, value: String) =
        runBlocking(Dispatchers.IO) {
            return@runBlocking httpClient.patch {
                url {
                    encodedPath = "/service/$serviceId/dictionary/$dictionaryId/item/$key"
                }
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("item_key", key)
                            append("item_value", value)
                        }
                    )
                )
            }.handle<DictionaryItem>()
        }

    /**
     * Update [DictionaryItem] in batch for given service, dictionary ID and key/value pairs for items.
     * @param serviceId The service id
     * @param dictionaryId The dictionary id
     * @param dictionaryUpdate the [DictionaryUpdate] to apply
     */
    fun updateDictionaryItems(serviceId: String, dictionaryId: String, dictionaryUpdate: DictionaryUpdate) =
        runBlocking(Dispatchers.IO) {
            httpClient.patch {
                url {
                    encodedPath = "/service/$serviceId/dictionary/$dictionaryId/items"
                }
                contentType(ContentType.Application.Json)
                setBody(dictionaryUpdate)
            }.handle<JsonElement>()
        }

    /**
     * Delete [DictionaryItem] given service, dictionary ID, and item key.
     * @param serviceId The service id
     * @param dictionaryId The dictionary id
     * @param key The item key
     */
    fun deleteDictionaryItem(serviceId: String, dictionaryId: String, key: String) = runBlocking(Dispatchers.IO) {
        httpClient.delete {
            url {
                encodedPath = "/service/$serviceId/dictionary/$dictionaryId/item/$key"
            }
        }.handleDelete()
    }
//endregion

//region Dictionary
    /**
     * Deletes a dictionary for a particular service and version.
     * @param serviceId The service id
     * @param version The service version
     * @param dictionaryName The dictionary to delete
     */
    fun deleteDictionary(serviceId: String, version: Int, dictionaryName: String) = runBlocking(Dispatchers.IO) {
        httpClient.delete {
            url {
                encodedPath = "/service/$serviceId/version/$version/dictionary/$dictionaryName"
            }
        }.handleDelete()
    }

    /**
     * Rename a [Dictionary] for a particular service and version.
     * @param serviceId The service id
     * @param version The service version
     * @param oldName The old dictionary name
     * @param newName The new dictionary name
     * @return The named [Dictionary]
     */
    fun renameDictionary(serviceId: String, version: Int, oldName: String, newName: String) =
        runBlocking(Dispatchers.IO) {
            return@runBlocking httpClient.put {
                url {
                    encodedPath = "/service/$serviceId/version/$version/dictionary/$oldName"
                }
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("name", newName)
                        }
                    )
                )
            }.handle<Dictionary>()
        }

    /**
     * Create named [Dictionary] for a particular service and version.
     * @param serviceId The service id
     * @param version The service version
     * @param dictionaryName The new dictionary name
     * @return The named [Dictionary]
     */
    fun createDictionary(serviceId: String, version: Int, dictionaryName: String, private: Boolean = false) =
        runBlocking(Dispatchers.IO) {
            return@runBlocking httpClient.post {
                url {
                    encodedPath = "/service/$serviceId/version/$version/dictionary"
                }
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("name", dictionaryName)
                            if (private) append("write_only", "true")
                        }
                    )
                )
            }.handle<Dictionary>()
        }

    /**
     * Retrieve a single [Dictionary] by name for the version and service.
     * @param serviceId The service id
     * @param version The service version
     * @param dictionaryName The dictionary name
     * @return The named [Dictionary] or null if the dictionary was not found
     */
    fun getDictionary(serviceId: String, version: Int, dictionaryName: String) = runBlocking(Dispatchers.IO) {
        return@runBlocking httpClient.get {
            url {
                encodedPath = "/service/$serviceId/version/$version/dictionary/$dictionaryName"
            }
        }.handleNullable<Dictionary?>()
    }

    /**
     * List all dictionaries for the version of the service.
     * @param serviceId The service id
     * @param version The service version
     * @return The list of all registered dictionaries
     */
    fun getDictionaries(serviceId: String, version: Int) = runBlocking(Dispatchers.IO) {
        return@runBlocking httpClient.get {
            url {
                encodedPath = "/service/$serviceId/version/$version/dictionary"
            }
        }.handleNullable<List<Dictionary>?>()
    }
//endregion

//region Service
    /**
     * Retrieves the [Service] identified by the passed serviceId
     * @param serviceId The id of the [Service] to request
     * @return The [Service] corresponding to the serviceId or null if the service was not found
     */
    fun getService(serviceId: String) = runBlocking(Dispatchers.IO) {
        return@runBlocking httpClient.get {
            url {
                encodedPath = "/service/$serviceId"
            }
        }.handleNullable<Service?>()
    }
//endregion

//region Version
    /**
     * Create a version for a particular service
     * @param serviceId The service id
     * @return The new created version
     */
    fun createVersion(serviceId: String) = runBlocking(Dispatchers.IO) {
        return@runBlocking httpClient.post {
            url {
                encodedPath = "/service/$serviceId/version"
            }
        }.handle<Version>()
    }
//endregion
}

private suspend inline fun HttpResponse.handleDelete(): JsonElement? {
    val code = status.value
    return if (code >= 400) {
        when (status) {
            HttpStatusCode.NotFound -> null
            else -> {
                val error = body<FastlyError>()
                throw FastlyException(error.message, code, error.detail)
            }
        }
    } else {
        body()
    }
}

private suspend inline fun <reified T> HttpResponse.handleNullable(): T? {
    if (request.method == HttpMethod.Get && status == HttpStatusCode.NotFound) {
        return null
    }

    if (contentLength() == null || contentLength()!! <= 0) {
        if (status.value >= 400) {
            throw FastlyException(
                message = null,
                status = status.value,
                detail = null,
                cause = null
            )
        }
    }

    return handle<T>()
}

private suspend inline fun <reified T> HttpResponse.handle(): T {
    val code = status.value

    if (code >= 400) {
        val error = this.body<FastlyError>()
        throw FastlyException(error.message, code, error.detail)
    } else {
        return body()
    }
}
