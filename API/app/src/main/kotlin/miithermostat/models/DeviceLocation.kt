package miithermostat.models

import miithermostat.getDb
import miithermostat.models.locationAssignements
import org.ktorm.dsl.*
import org.ktorm.schema.*
import kotlinx.serialization.*
import io.ktor.http.HttpStatusCode
import org.sqlite.SQLiteException
import org.postgresql.util.PSQLException

object DeviceLocation : Table<Nothing>("device_location") {
    val device_id = varchar("device_id")
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
        db.insert(DeviceLocation) {
            set(it.device_id, deviceId)
            set(it.location, location)
        }
        return HttpStatusCode.Created
    } catch (e: SQLiteException) {
        // Ignored as InsertOrUpdate not super supported.
    } catch (e: PSQLException) {
        // Ignored as InsertOrUpdate not super supported.
    }
    try {
        val updatedCount = db.update(DeviceLocation) {
            set(it.location, location)
            where {
                it.device_id eq deviceId
            }
        }
        if (updatedCount == 1) {
            return HttpStatusCode.OK
        }
    } catch (e: SQLiteException) {
        // Ignored as InsertOrUpdate not super supported.
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
    val unassignedDevices: MutableList<String> = mutableListOf(),
    val locations: MutableList<Location> = mutableListOf()
)

fun locationAssignements(): LocationAssignements {
    val locationMap: MutableMap<String, Location> = mutableMapOf()
    val unassignedDevices: MutableList<String> = mutableListOf()

    val db = getDb()
    db
    .from(Devices)
    .leftJoin(DeviceLocation, on = Devices.id eq DeviceLocation.device_id)
    .select(Devices.id, DeviceLocation.location)
    .orderBy(Devices.id.asc())
    .map { row ->
                val device = row[Devices.id]!!
                val locationName = row[DeviceLocation.location]

                if (locationName == null) {
                    unassignedDevices.add(device)
                } else {
                    var location = locationMap.get(locationName)
                    if (location == null) {
                        location = Location(locationName)
                        locationMap.put(locationName, location)
                    }
                    location.addDevice(Device(device))
                }
            }
    db
    .from(Locations)
    .select(Locations.name)
    .where { Locations.name.notInList(locationMap.keys) }
    .map {
        row ->
            val name = row[Locations.name]!!
            locationMap.put(name, Location(name))
    }
    return LocationAssignements(unassignedDevices = unassignedDevices, locations = locationMap.values)
}
