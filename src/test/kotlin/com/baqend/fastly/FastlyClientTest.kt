package com.baqend.fastly

import com.baqend.fastly.FastlyClientTest.HMethods.*
import com.baqend.fastly.model.DictionaryUpdate
import com.baqend.fastly.model.FastlyError
import com.baqend.fastly.model.FastlyException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import util.TestServer
import java.net.URI
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.text.get

class FastlyClientTest {
    @BeforeEach
    fun setupEach() {
        requestCount.set(0)
    }

    enum class HMethods {
        GET, POST, PATCH, PUT, DELETE
    }

    @ParameterizedTest
    @EnumSource
    @DisplayName("test fail directory after 3 retries with 409er")
    fun testFailDirectoryAfter3RetriesWith409er(method: HMethods) {
        val client = FastlyClient(URI("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val t = assertThrows<FastlyException> {
                    client.getDictionary(serviceId = "service1", dictionaryName = FASTLY_ERROR, version = 1)
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            POST -> {
                val t = assertThrows<FastlyException> {
                    client.createDictionary(serviceId = "service1", dictionaryName = FASTLY_ERROR, version = 1)
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            PUT -> {
                val t = assertThrows<FastlyException> {
                    client.renameDictionary(serviceId = "service1", oldName = FASTLY_ERROR, newName = "whatever", version = 1)
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            DELETE -> {
                val t = assertThrows<FastlyException> {
                    client.deleteDictionary(serviceId = "service1", dictionaryName = FASTLY_ERROR, version = 1)
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            else -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test fail DictioryItem after 3 retries with 409er`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val t = assertThrows<FastlyException> {
                    client.getDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = FASTLY_ERROR)
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            POST -> {
                val t = assertThrows<FastlyException> {
                    client.createDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = FASTLY_ERROR, value = "whatever")
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            PUT -> {
                val t = assertThrows<FastlyException> {
                    client.setDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = FASTLY_ERROR, value = "whatever")
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            DELETE -> {
                val t = assertThrows<FastlyException> {
                    client.deleteDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = FASTLY_ERROR)
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            PATCH -> {
                val t = assertThrows<FastlyException> {
                    client.updateDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = FASTLY_ERROR, value = "whatever")
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test fail directory items after 3 retries with 409er`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val t = assertThrows<FastlyException> {
                    client.getDictionaryItems(serviceId = "service1", dictionaryId = FASTLY_ERROR)
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            PATCH -> {
                val t = assertThrows<FastlyException> {
                    client.updateDictionaryItems(
                        serviceId = "service1", dictionaryId = "dict1",
                        dictionaryUpdate = DictionaryUpdate().update(
                            FASTLY_ERROR, "whatever"
                        )
                    )
                }
                assertEquals(409, t.status)
                assertEquals(FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            else -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test retry for service requests 500er`(method: HMethods) {
        val client = FastlyClient(URI("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val t = assertThrows<FastlyException> {
                    client.getService(serviceId = TEMP_FASTLY_ERROR)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            else -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test retry for version requests 500er`(method: HMethods) {
        val client = FastlyClient(URI("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            POST -> {
                val t = assertThrows<FastlyException> {
                    client.createVersion(serviceId = TEMP_FASTLY_ERROR)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            else -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test fail dircitonary requests after 3 retries with 500er`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val t = assertThrows<FastlyException> {
                    client.getDictionary(serviceId = "service1", dictionaryName = TEMP_FASTLY_ERROR, version = 1)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            POST -> {
                val t = assertThrows<FastlyException> {
                    client.createDictionary(serviceId = "service1", dictionaryName = TEMP_FASTLY_ERROR, version = 1)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            PUT -> {
                val t = assertThrows<FastlyException> {
                    client.renameDictionary(serviceId = "service1", oldName = TEMP_FASTLY_ERROR, newName = "whatever", version = 1)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            DELETE -> {
                val t = assertThrows<FastlyException> {
                    client.deleteDictionary(serviceId = "service1", dictionaryName = TEMP_FASTLY_ERROR, version = 1)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            else -> {
            } // ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test fail DictonaryItem requests after 3 retries with 500er`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val t = assertThrows<FastlyException> {
                    client.getDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = TEMP_FASTLY_ERROR)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            POST -> {
                val t = assertThrows<FastlyException> {
                    client.createDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = TEMP_FASTLY_ERROR, value = "whatever")
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            PUT -> {
                val t = assertThrows<FastlyException> {
                    client.setDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = TEMP_FASTLY_ERROR, value = "whatever")
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            DELETE -> {
                val t = assertThrows<FastlyException> {
                    client.deleteDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = TEMP_FASTLY_ERROR)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            PATCH -> {
                val t = assertThrows<FastlyException> {
                    client.updateDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = TEMP_FASTLY_ERROR, value = "whatever")
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test fail dircitonary items requests after 3 retries with 500er`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val t = assertThrows<FastlyException> {
                    client.getDictionaryItems(serviceId = "service1", dictionaryId = TEMP_FASTLY_ERROR)
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            PATCH -> {
                val t = assertThrows<FastlyException> {
                    client.updateDictionaryItems(
                        serviceId = "service1", dictionaryId = "dict1",
                        dictionaryUpdate = DictionaryUpdate().update(
                            TEMP_FASTLY_ERROR, "whatever"
                        )
                    )
                }
                assertEquals(503, t.status)
                assertEquals(TEMP_FASTLY_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
            }
            else -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `should not retry on 404er`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val dict = client.getDictionary(serviceId = "service1", dictionaryName = NOT_FOUND_ERROR, version = 1)
                assert(dict == null)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            POST -> {
                val t = assertThrows<FastlyException> {
                    client.createDictionary(serviceId = "service1", dictionaryName = NOT_FOUND_ERROR, version = 1)
                }
                assertEquals(404, t.status)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            PUT -> {
                val t = assertThrows<FastlyException> {
                    client.renameDictionary(serviceId = "service1", oldName = NOT_FOUND_ERROR, newName = "whatever", version = 1)
                }
                assertEquals(404, t.status)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            DELETE -> {
                assertNull(client.deleteDictionary(serviceId = "service1", dictionaryName = NOT_FOUND_ERROR, version = 1))
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            else -> {
            } // ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `should not retry DictionaryItem reqeusts on 404er`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val result = client.getDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = NOT_FOUND_ERROR)
                assertNull(result)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            POST -> {
                // Create Ignored
            }
            PUT -> {
                val t = assertThrows<FastlyException> {
                    client.setDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = NOT_FOUND_ERROR, value = "whatever")
                }
                assertEquals(404, t.status)
                assertEquals(NOT_FOUND_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            DELETE -> {
                assertNull(client.deleteDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = NOT_FOUND_ERROR))
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            PATCH -> {
                val t = assertThrows<FastlyException> {
                    client.updateDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = NOT_FOUND_ERROR, value = "whatever")
                }
                assertEquals(404, t.status)
                assertEquals(NOT_FOUND_ERROR_JSON.message, t.message)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `should not retry dictionary items reqeusts on 404er`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                assert(client.getDictionaryItems(serviceId = "service1", dictionaryId = NOT_FOUND_ERROR) == null)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            PATCH -> {
                val t = assertThrows<FastlyException> {
                    client.updateDictionaryItems(
                        serviceId = "service1", dictionaryId = "dict1",
                        dictionaryUpdate = DictionaryUpdate().update(
                            NOT_FOUND_ERROR, "whatever"
                        )
                    )
                }
                assertEquals(404, t.status)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            else -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test successful service requests`(method: HMethods) {
        val client = FastlyClient(URI("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val service = client.getService(serviceId = "service1")
                assert(service?.id == "service1")
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            else -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test successful version requests`(method: HMethods) {
        val client = FastlyClient(URI("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            POST -> {
                val version = client.createVersion(serviceId = "service1")
                assert(version.serviceId == "service1")
                assert(version.number == 1)
                assert(requestCount.get() == 1) { "Request count was ${requestCount.get()} instead of 1" }
            }
            else -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test successful dictionary requests`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val response = client.getDictionary(serviceId = "service1", dictionaryName = "dictionary5431", version = 1)
                assert(response?.name == "dictionary5431")
                assert(response?.serviceId == "service1")
                assert(response?.version == 1)
            }
            POST -> {
                val response = client.createDictionary(serviceId = "service1", version = 1, dictionaryName = "newDict")
                assert(response.name == "newDict")
                assert(response.version == 1)
                assert(response.serviceId == "service1")
            }
            PUT -> {
                val response = client.renameDictionary(serviceId = "service1", version = 1, oldName = "dictionary1", newName = "dictionary2")
                assert(response.name == "dictionary2")
                assert(response.version == 1)
                assert(response.serviceId == "service1")
            }
            DELETE -> {
                client.deleteDictionary(serviceId = "service1", version = 1, dictionaryName = "name")
            }
            PATCH -> {
            } // Ignored
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test successful dictionaryItem requests`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val item = client.getDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = "item1")
                assert(item?.key == "item1")
                assert(item?.value == "value1")
                assert(item?.serviceId == "service1")
                assert(item?.dictionaryId == "dict1")
            }
            POST -> {
                val response = client.createDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = "key1", value = "value134")
                assert(response.key == "key1")
                assert(response.value == "value134")
                assert(response.dictionaryId == "dict1")
                assert(response.serviceId == "service1")
            }
            PATCH -> {
                val response = client.updateDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = "item1", value = "value1")
                assert(response.key == "item1")
                assert(response.value == "value1")
                assert(response.dictionaryId == "dict1")
                assert(response.serviceId == "service1")
            }
            PUT -> {
                val response = client.setDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = "key1", value = "value1")
                assert(response.key == "key1")
                assert(response.value == "value1")
                assert(response.dictionaryId == "dict1")
                assert(response.serviceId == "service1")
            }
            DELETE -> {
                val response = client.deleteDictionaryItem(serviceId = "service1", dictionaryId = "dict1", key = "key1")
                assert(response?.asJsonObject?.get("status")?.asString == "ok")
            }
        }
    }

    @ParameterizedTest
    @EnumSource
    fun `test successful dictionary items requests`(method: HMethods) {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        when (method) {
            GET -> {
                val items = client.getDictionaryItems(serviceId = "service1", dictionaryId = "dict1")
                print(items)
                assert(items?.size == 2)
                items?.forEach {
                    assert(it.key == "item1" || it.key == "item2")
                    assert(it.value == "value1" || it.value == "value2")
                    assert(it.serviceId == "service1")
                    assert(it.dictionaryId == "dict1")
                }
            }
            PATCH -> {
                val update = DictionaryUpdate()
                update.create("key1", "value1")
                val updateItemsResponse = client.updateDictionaryItems(serviceId = "service1", dictionaryId = "dict1", dictionaryUpdate = update)
                assert(updateItemsResponse.asJsonObject.get("status").asString == "ok")
            }
            else -> {
            } // Ignored
        }
    }

    @Test
    fun `test empty string error response should not error`() {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        val exc = assertThrows<FastlyException> {
            val response = client.getService("emptyResponse")
            print(response)
        }
        assertNull(exc.detail)
        assertNull(exc.message)
        assertNull(exc.cause)
        assertEquals(503, exc.status)
        assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
    }

    @Test
    fun `test empty json error response should not error`() {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        val exc = assertThrows<FastlyException> {
            val response = client.getService("emptyJsonResponse")
            print(response)
        }
        assertNull(exc.detail)
        assertNull(exc.message)
        assertNull(exc.cause)
        assertEquals(503, exc.status)
        assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
    }

    @Test
    fun `test handling of text-json content type`() {
        val client = FastlyClient(URI.create("http://localhost:$SERVER_PORT"), arrayOf("1234"))
        val exc = assertThrows<FastlyException> {
            val response = client.getService("specialContentType")
            print(response)
        }
        assertNull(exc.detail)
        assertNull(exc.message)
        assertNull(exc.cause)
        assertEquals(503, exc.status)
        assert(requestCount.get() == 3) { "Request count was ${requestCount.get()} instead of 3" }
    }

    companion object {
        const val SERVER_PORT = 31080
        const val STATUS_OK = """{ "status":"ok" }"""
        const val FASTLY_ERROR = "fastlyError"
        const val TEMP_FASTLY_ERROR = "tempFastlyError"
        const val NOT_FOUND_ERROR = "notFound"
        val NOT_FOUND_ERROR_JSON = FastlyError("Not Found", "Bla not found")
        val FASTLY_ERROR_JSON = FastlyError("Fastly Error", "An error occured")
        val TEMP_FASTLY_ERROR_JSON = FastlyError("Internal Fastly Error", "An Internal error occured")
        lateinit var server: NettyApplicationEngine
        val requestCount = AtomicInteger()

        @BeforeAll
        @JvmStatic
        fun setup() {
            server = TestServer.initServer(SERVER_PORT) {
                // Print route resolution, helpful for debugging
                trace { application.log.trace(it.buildText()) }

                // Service
                route("/service/{serviceId}") {
                    get {
                        val serviceId = call.parameters["serviceId"]
                        when (serviceId) {
                            NOT_FOUND_ERROR -> call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                            TEMP_FASTLY_ERROR -> call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                            FASTLY_ERROR -> call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                            "emptyResponse" -> call.respondText("", ContentType.Application.Json, HttpStatusCode.ServiceUnavailable)
                            "emptyJsonResponse" -> call.respondText("{}", ContentType.Application.Json, HttpStatusCode.ServiceUnavailable)
                            "specialContentType" -> call.respondText("{}", ContentType("text", "json"), HttpStatusCode.ServiceUnavailable)
                            else -> {
                                call.respondText(contentType = ContentType.Application.Json) {
                                    """
                                        {
                                            "comment": "",
                                            "created_at": "2016-04-27T19:40:49+00:00",
                                            "customer_id": "x4xCwxxJxGCx123Rx5xTx",
                                            "deleted_at": null,
                                            "id": "$serviceId",
                                            "name": "test-service",
                                            "publish_key": "3c18bd0f5ada8f0cf54724d86c514a8eac4c9b75",
                                            "type": "vcl",
                                            "updated_at": "2016-04-27T19:40:49+00:00",
                                            "versions": [
                                                {
                                                    "active": null,
                                                    "backend": 1,
                                                    "comment": "",
                                                    "created_at": "2016-04-27T19:40:49",
                                                    "deleted_at": null,
                                                    "deployed": null,
                                                    "locked": "1",
                                                    "number": "1",
                                                    "service": "SU1Z0isxPaozGVKXdv0eY",
                                                    "service_id": "SU1Z0isxPaozGVKXdv0eY",
                                                    "staging": null,
                                                    "testing": null,
                                                    "updated_at": "2016-05-09T16:27:00"
                                                }
                                            ]
                                        }  
                                    """.trimIndent()
                                }
                            }
                        }
                        requestCount.incrementAndGet()
                    }

                    route("/version") {
                        post {
                            val serviceId = call.parameters["serviceId"]
                            when (serviceId) {
                                NOT_FOUND_ERROR -> call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                                TEMP_FASTLY_ERROR -> call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                                FASTLY_ERROR -> call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                                else -> {
                                    call.respondText(contentType = ContentType.Application.Json) {
                                        """
                                            {
                                              "number":1,
                                              "service_id": "$serviceId"
                                            } 
                                        """.trimIndent()
                                    }
                                }
                            }
                            requestCount.incrementAndGet()
                        }
                    }

                    // Version
                    route("/version/{version}") {
                        post {
                            val serviceId = call.parameters["serviceId"]
                            when (serviceId) {
                                NOT_FOUND_ERROR -> call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                                TEMP_FASTLY_ERROR -> call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                                FASTLY_ERROR -> call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                                else -> {
                                    call.respondText(contentType = ContentType.Application.Json) {
                                        """
                                            {
                                              "number":1,
                                              "service_id": "$serviceId"
                                            } 
                                        """.trimIndent()
                                    }
                                }
                            }
                            requestCount.incrementAndGet()
                        }

                        // Dictionary
                        route("/dictionary") {
                            post { handleDictionaryPostRequest(call) }
                            route("{dictionary?}") {
                                get { handleDictionaryRequest(call) }
                                put { handleDictionaryRequest(call) }
                                patch { handleDictionaryRequest(call) }
                                delete { handleDictionaryRequest(call) }
                            }
                        }
                    }
                }

                // Dictionary Items
                route("service/*/dictionary/{dictionary}") {
                    route("/item/{item?}") {
                        post { handleDictionaryItemRequest(call) }
                        get { handleDictionaryItemRequest(call) }
                        put { handleDictionaryItemRequest(call) }
                        patch { handleDictionaryItemRequest(call) }
                        delete { handleDictionaryItemRequest(call) }
                    }
                    route("/items") {
                        get { handleDictionaryItemsRequest(call) }
                        patch {
                            val body = call.receiveText()
                            println(body)
                            when {
                                body.contains(FASTLY_ERROR) -> {
                                    call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                                }
                                body.contains(TEMP_FASTLY_ERROR) -> {
                                    call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                                }
                                body.contains(NOT_FOUND_ERROR) -> {
                                    call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                                }
                                else -> {
                                    call.respondText(STATUS_OK, contentType = ContentType.Application.Json)
                                }
                            }
                            requestCount.incrementAndGet()
                        }
                    }
                }
            }.start(false)
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            // clean up after this class, leave nothing dirty behind
            server.stop(1000, 10000)
        }

        private suspend fun handleDictionaryPostRequest(call: ApplicationCall) {
            val params = call.receiveParameters()
            when (params["name"]) {
                NOT_FOUND_ERROR -> call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                TEMP_FASTLY_ERROR -> call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                FASTLY_ERROR -> call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                else -> handleSuccessfulResponse(call, params)
            }
            requestCount.incrementAndGet()
        }

        private suspend fun handleDictionaryRequest(call: ApplicationCall) {
            when (call.parameters["dictionary"]) {
                NOT_FOUND_ERROR -> call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                TEMP_FASTLY_ERROR -> call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                FASTLY_ERROR -> call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                else -> handleSuccessfulResponse(call)
            }
            requestCount.incrementAndGet()
        }

        private suspend fun handleSuccessfulResponse(call: ApplicationCall, parameters: Parameters? = null) {
            when (call.request.httpMethod) {
                HttpMethod.Get -> { // get dictionary
                    call.respondText(dictionaryJson(name = call.parameters["dictionary"]), contentType = ContentType.Application.Json)
                }
                HttpMethod.Post -> { // create dictionary
                    val postParams = parameters ?: call.receiveParameters()
                    call.respondText(dictionaryJson(postParams["name"]), contentType = ContentType.Application.Json)
                }
                HttpMethod.Put -> { // Rename Dictionary
                    val params = call.receiveParameters()
                    call.respondText(dictionaryJson(params["name"]), contentType = ContentType.Application.Json)
                }
                HttpMethod.Delete -> {
                    call.respondText(STATUS_OK, contentType = ContentType.Application.Json)
                }
            }
        }

        private fun dictionaryJson(name: String?): String {
            return """
                        {
                          "created_at": "${Instant.now()}",
                          "deleted_at": null,
                          "id": "dictionary1",
                          "name": $name,
                          "service_id": "service1",
                          "updated_at": "${Instant.now()}",
                          "version": 1,
                          "write_only": false
                        }
            """.trimIndent()
        }

        private fun dictionaryItemJson(key: String?, value: String?): String {
            return """
                    {
                      "dictionary_id": "dict1",
                      "service_id": "service1",
                      "item_key": "$key",
                      "item_value": "$value",
                      "created_at": "${Instant.now()}",
                      "deleted_at": null,
                      "updated_at": "${Instant.now()}"
                    }
            """.trimIndent()
        }

        private suspend fun handleDictionaryItemRequest(call: ApplicationCall) {
            if (call.parameters["item"] != null) {
                val item = call.parameters["item"]
                when (item) {
                    NOT_FOUND_ERROR -> call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                    TEMP_FASTLY_ERROR -> call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                    FASTLY_ERROR -> call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                    else -> handleSuccessfulDictionaryItemRequest(call)
                }
            } else {
                val params = call.receiveParameters()
                when (params["item_key"]) {
                    NOT_FOUND_ERROR -> call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                    TEMP_FASTLY_ERROR -> call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                    FASTLY_ERROR -> call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                    else -> handleSuccessfulDictionaryItemRequest(call, params)
                }
            }
            requestCount.incrementAndGet()
        }

        private suspend fun handleSuccessfulDictionaryItemRequest(call: ApplicationCall, parameters: Parameters? = null) {
            when (call.request.httpMethod) {
                HttpMethod.Get -> { // get dictionary
                    call.respondText(dictionaryItemJson(call.parameters["item"], "value1"), contentType = ContentType.Application.Json)
                }
                HttpMethod.Post -> { // create dictionary
                    val postParams = parameters ?: call.receiveParameters()
                    call.respondText(dictionaryItemJson(postParams["item_key"], postParams["item_value"]), contentType = ContentType.Application.Json)
                }
                HttpMethod.Put -> { // Rename Dictionary
                    val params = call.receiveParameters()
                    call.respondText(dictionaryItemJson(params["item_key"], params["item_value"]), contentType = ContentType.Application.Json)
                }
                HttpMethod.Patch -> { // Rename Dictionary
                    val params = call.receiveParameters()
                    call.respondText(dictionaryItemJson(params["item_key"], params["item_value"]), contentType = ContentType.Application.Json)
                }
                HttpMethod.Delete -> {
                    call.respondText(STATUS_OK, contentType = ContentType.Application.Json)
                }
            }
        }

        private suspend fun handleDictionaryItemsRequest(call: ApplicationCall) {
            when (call.parameters["dictionary"]) {
                NOT_FOUND_ERROR -> call.respond(HttpStatusCode.NotFound, NOT_FOUND_ERROR_JSON)
                TEMP_FASTLY_ERROR -> call.respond(HttpStatusCode.ServiceUnavailable, TEMP_FASTLY_ERROR_JSON)
                FASTLY_ERROR -> call.respond(HttpStatusCode.Conflict, FASTLY_ERROR_JSON)
                else -> handleSuccessfulDictionaryItemsRequest(call)
            }
            requestCount.incrementAndGet()
        }

        private suspend fun handleSuccessfulDictionaryItemsRequest(call: ApplicationCall) {
            when (call.request.httpMethod) {
                HttpMethod.Get -> { // get all items in dict1
                    call.respondText(
                        """
                        [
                            ${dictionaryItemJson("item1", "value1")},
                            ${dictionaryItemJson("item2", "value2")}
                        ]
                        """.trimIndent(),
                        contentType = ContentType.Application.Json
                    )
                }
                HttpMethod.Patch -> { // Do some updates
                    call.respondText(STATUS_OK, contentType = ContentType.Application.Json)
                }
                else -> TODO("Not implemented: ${call.request.httpMethod}")
            }
        }
    }
}
