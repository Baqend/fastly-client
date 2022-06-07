package util

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

class TestServer {
    companion object {
        fun initServer(portConfig: Int, configuration: Routing.() -> Unit): NettyApplicationEngine {
            val env = applicationEngineEnvironment {
                module {
                    install(DefaultHeaders)
                    install(CallLogging) {
                        level = Level.TRACE
                    }
                    install(ContentNegotiation) {
                        gson {
                            serializeNulls()
                        }
                    }
                    routing(configuration)
                }
                // Public API
                connector {
                    host = "0.0.0.0"
                    port = portConfig
                }
            }
            return embeddedServer(Netty, env)
        }
    }
}
