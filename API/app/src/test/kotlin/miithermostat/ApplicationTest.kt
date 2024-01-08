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
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import miithermostat.models.*
import miithermostat.plugins.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.ktorm.dsl.*

private val TEST_DB_HOST = System.getenv("TEST_DB_HOST")
private val TEST_DB_NAME = System.getenv("TEST_DB_NAME")
private val TEST_DB_USER = System.getenv("TEST_DB_USER")
private val TEST_DB_PASSWORD = System.getenv("TEST_DB_PASSWORD")


private val db = Database.connect(
            url = "jdbc:postgresql://${TEST_DB_HOST}/${TEST_DB_NAME}",
            user = TEST_DB_USER,
            password = TEST_DB_PASSWORD
        )

class ApplicationTest {
    @BeforeEach
    fun setUp() {
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
        assertEquals(data.size, 22)
        val measure1 = data[0]
        assertEquals(TEST_LOCATION2, measure1.location)
        assertEquals(199, measure1.temperature_mc)
        assertEquals(399, measure1.humidity_pt)
        val measure2 = data[1]
        assertEquals(TEST_LOCATION, measure2.location)
        assertEquals(305, measure2.temperature_mc)
        assertEquals(250, measure2.humidity_pt)
        val measure21 = data[20]
        assertEquals(TEST_LOCATION2, measure21.location)
        assertEquals(299, measure21.temperature_mc)
        assertEquals(299, measure21.humidity_pt)
        val measure22 = data[21]
        assertEquals(TEST_LOCATION, measure22.location)
        assertEquals(205, measure22.temperature_mc)
        assertEquals(350, measure22.humidity_pt)
    }

    @Test
    fun testMeasurementsGetLastDayData() = testApplication {
        createSparseMeasurements()

        val response = client.get("/measurements/lastday")

        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<SensorData>>(response.bodyAsText())
        assertEquals(2, data.size)
        val measure2 = data[0]
        assertEquals(TEST_LOCATION, measure2.location)
        assertEquals(305, measure2.temperature_mc)
        assertEquals(250, measure2.humidity_pt)
        val measure1 = data[1]
        assertEquals(TEST_LOCATION2, measure1.location)
        assertEquals(199, measure1.temperature_mc)
        assertEquals(399, measure1.humidity_pt)
    }

    @Test
    fun testMeasurementsGetLast3DaysData() = testApplication {
        createSparseMeasurements()

        val response = client.get("/measurements/last3days")

        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<SensorData>>(response.bodyAsText())
        assertEquals(6, data.size)
        val measure2 = data[0]
        assertEquals(TEST_LOCATION, measure2.location)
        assertEquals(285, measure2.temperature_mc)
        assertEquals(270, measure2.humidity_pt)
        val measure1 = data[1]
        assertEquals(TEST_LOCATION2, measure1.location)
        assertEquals(219, measure1.temperature_mc)
        assertEquals(379, measure1.humidity_pt)
    }

    @Test
    fun testMeasurementsPost() = testApplication {
        val response =
                client.post("/measurements") {
                    contentType(ContentType.Application.Json)
                    setBody(
                            String.format(
                                    "{\"device_id\":\"%s\",\"humidity_pt\":240,\"temperature_mc\":234}",
                                    TEST_DEVICE
                            )
                    )
                }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Success", response.bodyAsText())
        val measurements = getAllSensorData()
        assertEquals(measurements.size, 1)
    }

    @Test
    fun testDevicesGet() = testApplication {
        val response = client.get("/devices/")

        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<Device>>(response.bodyAsText())
        assertContentEquals(
                listOf(TEST_DEVICE3, TEST_DEVICE6, TEST_DEVICE, TEST_DEVICE5, TEST_DEVICE2),
                data.map { device -> device.id }
        )
        assertContentEquals(
                listOf(TEST_LOCATION2, null, TEST_LOCATION, null, TEST_LOCATION2),
                data.map { device -> device.location }
        )
    }

    @Test
    fun testDevicesPost() = testApplication {
        val deviceId = TEST_DEVICE4
        val response =
                client.post("/devices/") {
                    contentType(ContentType.Application.Json)
                    setBody(String.format("{\"id\":\"%s\"}", deviceId))
                }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Created Device", response.bodyAsText())
        val db = getDb()
        val devices =
                db.from(Devices).select().where { Devices.id eq deviceId }.map { row ->
                    Device(row[Devices.id]!!)
                }
        assertEquals(deviceId, devices[0].id)
    }

    @Test
    fun testLocationsGet() = testApplication {
        createTestMeasurements()
        val response = client.get("/locations/")

        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<List<Location>>(response.bodyAsText())
        assertContentEquals(
                listOf(TEST_LOCATION, TEST_LOCATION2),
                data.map { location -> location.name }
        )
        val location1 = data[0]
        assertContentEquals(listOf(TEST_DEVICE), location1.devices.map { device -> device.id })
        assertEquals(205, location1.data?.temperature_mc)
        assertEquals(350, location1.data?.humidity_pt)
        assertEquals(Instant.fromEpochMilliseconds(LATEST_TIME_LOCATION), location1.data?.time)
        val location2 = data[1]
        assertContentEquals(
                listOf(TEST_DEVICE3, TEST_DEVICE2),
                location2.devices.map { device -> device.id }
        )
    }

    @Test
    fun testOffsetPost() = testApplication {
        val deviceId = TEST_DEVICE6
        val response =
                client.post(String.format("/devices/%s/offset", deviceId)) {
                    contentType(ContentType.Application.Json)
                    setBody(String.format("{\"temperature_mc_offset\":10,\"humidity_pt_offset\":-10}", ))
                }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Offset created", response.bodyAsText())
        val db = getDb()
        val devicesOffset =
                db.from(DevicesOffset)
                .select()
                .where { DevicesOffset.device_id eq deviceId }
                .map { row ->
                    DeviceOffset(row[DevicesOffset.temperature_mc_offset]!!, row[DevicesOffset.humidity_pt_offset]!!)
                }
        assertEquals(10, devicesOffset[0].temperature_mc_offset)
        assertEquals(-10, devicesOffset[0].humidity_pt_offset)
    }

    @Test
    fun testLocationAssignements() = testApplication {
        // Adding unassigned device and empty room
        insertDevice(TEST_DEVICE4)
        insertLocation(TEST_LOCATION3)

        val response =
                client.get("/assignements/") {
                    contentType(ContentType.Application.Json)
                }

        assertEquals(HttpStatusCode.OK, response.status)
        val data = Json.decodeFromString<LocationAssignements>(response.bodyAsText())
        assertEquals<MutableList<Location>>(mutableListOf(
            Location(name=TEST_LOCATION, devices=mutableListOf(Device(id=TEST_DEVICE, location=TEST_LOCATION)), data=null),
            Location(name=TEST_LOCATION2, devices=mutableListOf(Device(id=TEST_DEVICE3, location=TEST_LOCATION2), Device(id=TEST_DEVICE2, location=TEST_LOCATION2)), data=null),
            Location(name=TEST_LOCATION3, devices=mutableListOf(), data=null)
        ), data.locations)
        assertEquals(mutableListOf(Device(TEST_DEVICE6), Device(TEST_DEVICE4), Device(TEST_DEVICE5)), data.unassignedDevices)
    }

    @Test
    fun testLocationDeletionWithAssignedDevices() = testApplication {
        createTestMeasurements()

        val response = client.delete(String.format("/locations/%s/", TEST_LOCATION2))

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testLocationDeletionWithoutAssignedDevices() = testApplication {
        val testLocation = "new location"
        insertLocation(testLocation)

        val response = client.delete(String.format("/locations/%s/", testLocation))

        assertEquals(HttpStatusCode.OK, response.status)
    }
}
