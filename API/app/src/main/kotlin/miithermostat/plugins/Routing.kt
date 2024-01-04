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
            val statusCode = sensorData.save()
            var text = ""
            when(statusCode) {
                HttpStatusCode.Created -> text = "Success"
                HttpStatusCode.BadRequest -> text = "Device has no assigned location or not registered"
                HttpStatusCode.NotFound -> text = "Device missing from payload"
            }
            call.respondText(text, status = statusCode)
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
            val statusCode = device.save()
            var text = ""
            when(statusCode) {
                HttpStatusCode.Created -> text = "Created Device"
                HttpStatusCode.OK -> text = "Device already exists"
            }
            call.respondText(text, status = statusCode)
        }

        post("/devices/{device}/offset") {
            val deviceOffset = call.receive<DeviceOffset>()
            val deviceId = call.parameters["device"]!!
            val statusCode = setDeviceOffset(deviceId, deviceOffset)
            var text = ""
            when(statusCode) {
                HttpStatusCode.Created -> text = "Offset created"
                HttpStatusCode.OK -> text = "Offset updated"
                HttpStatusCode.NotFound -> text = "Device not found"
            }
            call.respondText(text, status = statusCode)
        }

        get("/locations/") {
            call.respond<List<Location>>(getAllLocations())
        }

        post("/locations/") {
            val location = call.receive<Location>()
            val statusCode = location.save()
            call.respondText("", status = statusCode)
        }

        post("/locations/{location}/devices/") {
            val location = call.parameters["location"]!!
            val device = call.receive<Device>()
            val statusCode = insertDeviceLocation(device.id, location)
            
            var text = ""
            when(statusCode) {
                HttpStatusCode.Created -> text = "Linked Device to Location"
                HttpStatusCode.OK -> text = "Update Device to be linked with Location"
                HttpStatusCode.NotFound -> text = "Location or Device not found"
            }
            call.respondText(text, status = statusCode)
        }

        get("/locations/{location}/data") {
            // TODO
        }

        get("/assignements/") {
            call.respond<LocationAssignements>(getLocationAssignements())
        }
    }
}