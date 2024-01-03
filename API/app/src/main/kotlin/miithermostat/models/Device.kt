package miithermostat.models

import miithermostat.getDb
import kotlinx.serialization.*
import org.ktorm.dsl.*
import org.ktorm.schema.*
import org.ktorm.support.postgresql.insertOrUpdate
import org.postgresql.util.PSQLException
import io.ktor.http.HttpStatusCode

@Serializable
data class Device(val id: String, val location: String? = null) {
    fun save(): HttpStatusCode {
        return insertDevice(id)
    }
}

object Devices : Table<Nothing>("device") {
    val id = varchar("id").primaryKey()
}

fun insertDevice(id: String): HttpStatusCode {
    try {
        val db = getDb()
        db.insertOrUpdate(Devices) {
            set(it.id, id)
            onConflict {
                doNothing()
            }
        }
        return HttpStatusCode.Created
    } catch (e: PSQLException) {
        // Ignored as InsertOrUpdate not super supported.
    }
    return HttpStatusCode.OK
}

fun getAllDevices(): List<Device> {
    val db = getDb()
    return db
    .from(Devices)
    .leftJoin(DeviceLocation, on = Devices.id eq DeviceLocation.device_id)
    .select(Devices.id, DeviceLocation.location)
    .orderBy(Devices.id.asc())
    .map { row ->
        Device(
            id = row[Devices.id]!!,
            location = row[DeviceLocation.location]
        )
    }
}

