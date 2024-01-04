package miithermostat.plugins

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
            }
        )
    }
}