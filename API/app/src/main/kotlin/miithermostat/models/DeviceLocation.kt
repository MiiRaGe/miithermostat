package miithermostat.models

import miithermostat.getDb
import org.ktorm.dsl.*
import org.ktorm.schema.*
import kotlinx.serialization.*
import io.ktor.http.HttpStatusCode
import org.ktorm.support.postgresql.insertOrUpdate
import org.postgresql.util.PSQLException

object DeviceLocation : Table<Nothing>("device_location") {
    val device_id = varchar("device_id").primaryKey()
    val location = varchar("location")
}

fun getLocation(deviceId: String): String? {
    val db = getDb()
    val query =
            db.from(DeviceLocation).select(DeviceLocation.location).where {
                (DeviceLocation.device_id eq deviceId)
            }

    for (row in query) {
        return row.getString(1)
    }
    return null
}

fun insertDeviceLocation(deviceId: String, location: String): HttpStatusCode {
    val db = getDb()
    try {
        db.insertOrUpdate(DeviceLocation) {
            set(it.device_id, deviceId)
            set(it.location, location)
            onConflict {
                set(it.location, location)
            }
        }
        return HttpStatusCode.OK
    } catch (e: PSQLException) {
        // Ignored as InsertOrUpdate not super supported.
    }
    return HttpStatusCode.NotFound
}

fun getDevicesByLocation(location: String): List<Device> {
    val db = getDb()
    return db.from(DeviceLocation)
            .select(DeviceLocation.device_id)
            .where { (DeviceLocation.location eq location) }
            .orderBy(DeviceLocation.device_id.asc())
            .map { row -> Device(row[DeviceLocation.device_id]!!) }
}

@Serializable
data class LocationAssignements(
    val unassignedDevices: MutableList<Device> = mutableListOf(),
    val locations: MutableList<Location> = mutableListOf()
) {
    fun getUnassignedDeviceIds(): List<String> {
        return unassignedDevices.map { device -> device.id };
    }
    fun save(): HttpStatusCode {
        val db = getDb();
        if (unassignedDevices.size > 0) {
            db.delete(DeviceLocation, { DeviceLocation.device_id.inList(getUnassignedDeviceIds()) });
        }
        for (location in locations) {
            for (device in location.devices) {
                insertDeviceLocation(device.id, location.name)    
            }
        }
        return HttpStatusCode.OK;
    }
}

fun getLocationAssignements(): LocationAssignements {
    val locationMap: MutableMap<String, Location> = mutableMapOf()
    val unassignedDevices: MutableList<Device> = mutableListOf()

    val db = getDb()
    val rowSet = db.useConnection { conn ->
        conn.prepareStatement("""
        select device.id as device_id, "location"."name" as location_name
        from device_location
        full outer join device on device.id = device_location.device_id
        full outer join location on location.name = device_location.location
        order by "location"."name", device.id
        """).executeQuery()
    }
    while (rowSet.next()) {
        val device = rowSet.getString("device_id")
        val locationName = rowSet.getString("location_name")

        if (locationName == null) {
            if (device != null) {
                unassignedDevices.add(Device(device))
            }
        } else {
            var location = locationMap.get(locationName);
            if (location == null) {
                location = Location(locationName)
                locationMap.put(locationName, location)
            }
            if (device != null) {
                location.addDevice(Device(device, locationName))    
            }
        }
    }
    rowSet.close()
    return LocationAssignements(unassignedDevices = unassignedDevices, locations = locationMap.values.toMutableList())
}
