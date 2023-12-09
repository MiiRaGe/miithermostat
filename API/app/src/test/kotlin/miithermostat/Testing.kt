package miithermostat

import kotlinx.datetime.Instant
import miithermostat.getDb
import miithermostat.models.*

fun dropTestTables() {
    val db = getDb()
    db.useConnection { conn ->
        var sql = """
        drop table IF EXISTS device_location;
        """
        conn.prepareStatement(sql).execute().toString()
        
        sql = """
        drop table IF EXISTS sensor_data;
        """
        conn.prepareStatement(sql).execute().toString()
        
        sql = """
        drop table IF EXISTS location;
        """
        conn.prepareStatement(sql).execute().toString()
        
        sql = """
        drop table IF EXISTS device;
        """
        conn.prepareStatement(sql).execute().toString()
    }
}

val TEST_DEVICE = "c4d6"
val TEST_DEVICE2 = "ffe4"
val TEST_DEVICE3 = "acbd"
val TEST_DEVICE4 = "cccc"
val TEST_LOCATION = "room"
val TEST_LOCATION2 = "room2"

fun createTestData() {
    insertLocation(TEST_LOCATION)
    insertLocation(TEST_LOCATION2)
    insertDevice(TEST_DEVICE)
    insertDevice(TEST_DEVICE2)
    insertDevice(TEST_DEVICE3)
    insertDeviceLocation(TEST_DEVICE, TEST_LOCATION)  
    insertDeviceLocation(TEST_DEVICE2, TEST_LOCATION2)
    insertDeviceLocation(TEST_DEVICE3, TEST_LOCATION2)
}

fun createTestMeasurements() {
    SensorData(device_id = TEST_DEVICE2, temperature_mc = 200, humidity = 40, time= Instant.fromEpochMilliseconds(0)).save()
    SensorData(device_id = TEST_DEVICE, temperature_mc = 300, humidity = 20).save()
}