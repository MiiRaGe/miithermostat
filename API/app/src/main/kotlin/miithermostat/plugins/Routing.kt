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
        post("/measurements") {
            val sensorData = call.receive<SensorData>()
            sensorData.save()
            call.respondText("", status = HttpStatusCode.Created)
        }

        get("/measurements") {
            call.respond<List<SensorData>>(getAllSensorData())
        }

        get("/measurements/lastday") {
            val from = System.now().minus(1.days)
            call.respond<List<SensorData>>(getSensorData(from))
        }

        get("/measurements/last3days") {
            val from = System.now().minus(3.days)
            call.respond<List<SensorData>>(getSensorData(from))
        }

        get("/measurements/lastweek") {         
            val from = System.now().minus(7.days)
            call.respond<List<SensorData>>(getSensorData(from))
        }

        get("/measurements/lastmonth") {
            val from = System.now().minus(30.days)
            call.respond<List<SensorData>>(getSensorData(from))
        }

        get("/devices/") {
            call.respond<List<Device>>(getAllDevices())
        }

        post("/devices/") {
            val device = call.receive<Device>()
            device.save()
            call.respondText("", status = HttpStatusCode.Created)
        }

        get("/devices/{deviceId}/locations/") {
            val deviceId = call.parameters["deviceId"]!!
            val location = getLocation(deviceId)
            if (location == null) {
                call.respond<List<String>>(listOf())    
            } else {
                call.respond<List<String>>(listOf(location))
            }
        }

        get("/locations/{location}/devices/") {
            val location = call.parameters["location"]!!
            call.respond<List<Device>>(getDevicesByLocation(location))    
        }

        get("/locations/{location}/data/lastmonth") {
            // TODO
        }
    }
}