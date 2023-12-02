package miithermostat.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.logging.toLogString
import miithermostat.models.*
import kotlinx.datetime.Clock.System
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.days

fun Application.configureRouting() {
    routing {
        get("/measurements") {
            call.respond<List<SensorData>>(getAllSensorData())
        }

        get("/measurements/last3days") {
            val to = System.now();
            val from = to.minus(3.days)
            call.respond<List<SensorData>>(getSensorData(from, to))
        }

        get("/measurements/lastweek") {         
            val to = System.now();
            val from = to.minus(7.days)
            call.respond<List<SensorData>>(getSensorData(from, to))
        }

        get("/measurements/lastmonth") {
            val to = System.now();
            val from = to.minus(30.days)
            call.respond<List<SensorData>>(getSensorData(from, to))
        }

        get("/location/{location}/lastmonth") {
            val to = System.now();
            val from = to.minus(30.days)
            call.respond<List<SensorData>>(getSensorData(from, to, call.parameters["location"]))
        }

        post("/measurements") {
            val sensorData = call.receive<SensorData>()
            sensorData.save()
            call.respondText("", status = HttpStatusCode.Created)
        }
    }
}