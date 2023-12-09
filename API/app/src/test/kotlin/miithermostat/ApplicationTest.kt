/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package miithermostat

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import java.sql.Connection
import java.sql.DriverManager
import kotlin.concurrent.thread
import kotlin.test.*
import kotlinx.serialization.json.Json
import miithermostat.getDb
import miithermostat.models.*
import miithermostat.plugins.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.ktorm.database.Database
import org.ktorm.dsl.*

object Global {
    // Create a connection when it's first used.
    val connection = DriverManager.getConnection("jdbc:sqlite::memory:")

    init {
        // Add a shutdown hook to close the connection when the application exits.
        Runtime.getRuntime().addShutdownHook(thread(start = false) { connection.close() })
    }
}

class ApplicationTest {
    @BeforeEach
    fun setUp() {
        val db =
                Database.connect {
                    object : Connection by Global.connection {
                        override fun close() {
                            // Override the close function and do nothing, keep the connection open.
                        }
                    }
                }
        setDb(db)
        createTables()
        createTestData()
    }

    @AfterEach
    fun tearDown() {
        dropTestTables()
    }

    @Test
    fun testMeasurementsGetEmpty() = testApplication {
        val response = client.get("/measurements")

        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<SensorData>>(response.bodyAsText())
        assertEquals(data.size, 0)
    }

    @Test
    fun testMeasurementsGetData() = testApplication {
        createTestMeasurements()

        val response = client.get("/measurements")

        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<SensorData>>(response.bodyAsText())
        assertEquals(data.size, 2)
        val measure1 = data[0]
        assertEquals(measure1.location, TEST_LOCATION2)
        assertEquals(measure1.temperature_mc, 200)
        assertEquals(measure1.humidity, 40)
        val measure2 = data[1]
        assertEquals(measure2.location, TEST_LOCATION)
        assertEquals(measure2.temperature_mc, 300)
        assertEquals(measure2.humidity, 20)
    }

    @Test
    fun testMeasurementsPost() = testApplication {
        val response =
                client.post("/measurements") {
                    contentType(ContentType.Application.Json)
                    setBody(
                            String.format(
                                    "{\"device_id\":\"%s\",\"humidity\":24,\"temperature_mc\":234}",
                                    TEST_DEVICE
                            )
                    )
                }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("", response.bodyAsText())
        val measurements = getAllSensorData()
        assertEquals(measurements.size, 1)
    }

    @Test
    fun testDevicesPost() = testApplication {
        val deviceId = TEST_DEVICE4
        val response =
                client.post("/devices/") {
                    contentType(ContentType.Application.Json)
                    setBody(String.format("{\"id\":\"%s\"}", deviceId))
                }
        client.post("/devices/") {
                    contentType(ContentType.Application.Json)
                    setBody(String.format("{\"id\":\"%s\"}", deviceId))
                }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("", response.bodyAsText())
        val db = getDb()
        val devices = db.from(Devices).select().where { Devices.id eq deviceId }.map {
            row -> Device(row[Devices.id]!!)
        }
        assertEquals(deviceId, devices[0].id)
    }

    @Test
    fun testDevicesGet() = testApplication {
        val response = client.get("/devices/")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<Device>>(response.bodyAsText())
        assertContentEquals(listOf(TEST_DEVICE3, TEST_DEVICE, TEST_DEVICE2), data.map { device -> device.id})
    }
    
    @Test
    fun testDevicesLocationsGet() = testApplication {
        val response = client.get(String.format("/devices/%s/locations/", TEST_DEVICE))
        
        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<String>>(response.bodyAsText())
        assertEquals(1, data.size)
        assertEquals(TEST_LOCATION, data[0])
    }
    
    @Test
    fun testDevicesByLocationsGet() = testApplication {
        val response = client.get(String.format("/locations/%s/devices/", TEST_LOCATION2))
        
        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<Device>>(response.bodyAsText())
        assertContentEquals(listOf(TEST_DEVICE3, TEST_DEVICE2), data.map { device -> device.id})
    }
}
