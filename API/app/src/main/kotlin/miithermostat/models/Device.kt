package miithermostat.models

import miithermostat.getDb
import org.ktorm.dsl.*
import org.ktorm.schema.*

object Device : Table<Nothing>("device") {
    val id = varchar("id")
}

fun insertDevice(id: String) {
    val db = getDb()
    db.insert(Device) {
        set(it.id, id)
    }
}