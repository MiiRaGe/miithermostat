package miithermostat.models

import miithermostat.getDb
import kotlinx.serialization.*
import org.ktorm.dsl.*
import org.ktorm.schema.*
import org.ktorm.entity.*
import org.sqlite.*

@Serializable
data class Device(val id: String) {
    fun save() {
        val db = getDb()
        db.insert(Devices) {
            set(it.id, id)
        }
    }
}

object Devices : Table<Nothing>("device") {
    val id = varchar("id").primaryKey()
}

fun insertDevice(id: String) {
    try {
        val db = getDb()
        db.insert(Devices) {
            set(it.id, id)
        }
    }
    catch (e: Exception) {
        println("Conflict on inserting device")
    }
}

fun getAllDevices(): List<Device> {
    val db = getDb()
    return db.from(Devices).select().orderBy(Devices.id.asc()).map { row ->
        Device(
            id = row[Devices.id]!!
        )
    }
}