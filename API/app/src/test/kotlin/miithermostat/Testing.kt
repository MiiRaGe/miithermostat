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
val TEST_DEVICE5 = "eeee"
val TEST_LOCATION = "room"
val TEST_LOCATION2 = "room2"
val LATEST_TIME_LOCATION = 601000L
val LATEST_TIME_LOCATION2 = 600000L

fun createTestData() {
    insertLocation(TEST_LOCATION)
    insertLocation(TEST_LOCATION2)
    insertDevice(TEST_DEVICE)
    insertDevice(TEST_DEVICE2)
    insertDevice(TEST_DEVICE3)
    insertDevice(TEST_DEVICE5)
    
    insertDeviceLocation(TEST_DEVICE, TEST_LOCATION)  
    insertDeviceLocation(TEST_DEVICE2, TEST_LOCATION2)
    insertDeviceLocation(TEST_DEVICE3, TEST_LOCATION2)
}

fun createTestMeasurements() {
    for (i in 0..10) {
        val timeMs: Long = (i * 60000).toLong()
        val humitidy1 = 40 - i
        val humidity2 = 25 + i
        val temperature_mc1 = 200 + 10 * i
        val temperature_mc2 = 305 - 10 * i
        SensorData(device_id = TEST_DEVICE2, temperature_mc = temperature_mc1.toShort(), humidity = humitidy1.toShort(), time=Instant.fromEpochMilliseconds(timeMs)).save()
        SensorData(device_id = TEST_DEVICE, temperature_mc = temperature_mc2.toShort(), humidity = humidity2.toShort(), time=Instant.fromEpochMilliseconds(timeMs + 1000)).save()
    }
}