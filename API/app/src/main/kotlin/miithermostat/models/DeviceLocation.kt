package miithermostat.models

import miithermostat.getDb
import org.ktorm.dsl.*
import org.ktorm.schema.*

object DeviceLocation : Table<Nothing>("device_location") {
    val device_id = varchar("device_id")
    val location = varchar("location")
}

fun getLocation(deviceId: String): String? {
    val db = getDb()
    val query = db.from(DeviceLocation).select(DeviceLocation.location)
    .where { (DeviceLocation.device_id eq deviceId) }
    
    for (row in query) {
        return row.getString(1);
    }
    return null;
}

fun insertDeviceLocation(deviceId: String, location: String) {
    val db = getDb()
    db.insert(DeviceLocation) {
        set(it.device_id, deviceId)
        set(it.location, location)
    }
}