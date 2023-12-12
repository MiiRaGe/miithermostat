package miithermostat.models

import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import miithermostat.getDb
import org.ktorm.dsl.*
import org.ktorm.schema.*
import org.postgresql.util.PSQLException
import org.sqlite.SQLiteException

@Serializable
data class Location(
        val name: String,
        val devices: MutableList<Device> = mutableListOf(),
        var data: SensorData? = null
) {
    fun addDevice(device: Device) {
        devices.add(device)
    }

    fun save(): HttpStatusCode {
        return insertLocation(name)
    }
}

object Locations : Table<Nothing>("location") {
    val name = varchar("name")
}

fun insertLocation(name: String): HttpStatusCode {
    try {
        val db = getDb()
        db.insert(Locations) { set(it.name, name) }
        return HttpStatusCode.Created
    } catch (e: SQLiteException) {
        // Ignored as InsertOrUpdate not super supported.
    } catch (e: PSQLException) {
        // Ignored as InsertOrUpdate not super supported.
    }
    return HttpStatusCode.OK
}

fun getAllLocations(): List<Location> {
    val db = getDb()
    val latestSensorDataMap = getLatestSensorDataByLocation()
    val locationMap: MutableMap<String, Location> = mutableMapOf()
    db.from(Locations)
            .leftJoin(DeviceLocation, on = Locations.name eq DeviceLocation.location)
            .select()
            .orderBy(DeviceLocation.device_id.asc())
            .map { row ->
                val locationName = row[Locations.name]!!
                val device = row[DeviceLocation.device_id]

                var location = locationMap.get(locationName)
                if (location == null) {
                    location = Location(locationName)
                    locationMap.put(locationName, location)
                }
                if (device != null) {
                    location.addDevice(Device(device))
                }
                locationMap.put(locationName, location)
            }
    for (key in locationMap.keys) {
        val latestSensorData = latestSensorDataMap.get(key)
        if (latestSensorData != null) {
            locationMap.get(key)?.data = latestSensorData
        } 
    }
    return locationMap.values.sortedBy { it -> it.name }
    
}
