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
val TEST_LOCATION = "room"
val TEST_DEVICE2 = "afe4"
val TEST_LOCATION2 = "room2"

fun createTestData() {
    insertLocation(TEST_LOCATION)
    insertDevice(TEST_DEVICE)
    insertDeviceLocation(TEST_DEVICE, TEST_LOCATION)
}

fun createTestMeasurements() {
    insertLocation(TEST_LOCATION2)
    insertDevice(TEST_DEVICE2)
    insertDeviceLocation(TEST_DEVICE2, TEST_LOCATION2)
    SensorData(device_id = TEST_DEVICE2, temperature_mc = 200, humidity = 40, time= Instant.fromEpochMilliseconds(0)).save()
    SensorData(device_id = TEST_DEVICE, temperature_mc = 300, humidity = 20).save()
}