package miithermostat.models

import miithermostat.getDb
import org.ktorm.dsl.*
import org.ktorm.schema.*

object Location : Table<Nothing>("location") {
    val name = varchar("name")
}

fun insertLocation(name: String) {
    val db = getDb()
    db.insert(Location) {
        set(it.name, name)
    }
}
