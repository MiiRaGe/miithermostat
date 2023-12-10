package miithermostat.models

import kotlinx.datetime.Instant
import kotlinx.serialization.*
import miithermostat.getDb
import miithermostat.tools.convert
import org.ktorm.dsl.*
import org.ktorm.schema.*
import io.ktor.http.HttpStatusCode

@Serializable
data class SensorData(
        val device_id: String? = null,
        val temperature_mc: Short,
        val humidity: Short,
        val time: Instant? = null,
        val location: String? = null,
) {
    fun save(): HttpStatusCode {
        if (device_id == null) {
            return HttpStatusCode.NotFound
        }
        val location = getLocation(device_id)
        println(String.format("Cannot find location for : %s", device_id))
        if (location == null) {
            return HttpStatusCode.BadRequest
        }
        val db = getDb()
        db.insert(Conditions) {
            set(it.device_id, device_id)
            set(it.temperature_mc, temperature_mc)
            set(it.humidity, humidity)
            set(it.location, location)
        }
        return HttpStatusCode.Created
    }
}

object Conditions : Table<Nothing>("sensor_data") {
    val id = int("id").primaryKey()
    val device_id = varchar("device_id")
    val temperature_mc = short("temperature_mc")
    val humidity = short("humidity")
    val time = timestamp("time")
    val location = varchar("location")
}

fun getAllSensorData(): List<SensorData> {
    val db = getDb()
    return db.from(Conditions).select().orderBy(Conditions.time.asc()).map { row ->
        SensorData(
                location = row[Conditions.location]!!,
                temperature_mc = row[Conditions.temperature_mc]!!,
                humidity = row[Conditions.humidity]!!,
                time = Instant.fromEpochMilliseconds(row[Conditions.time]?.toEpochMilli() ?: 0)
        )
    }
}

fun getSensorData(from: Instant, to: Instant? = null, location: String? = null): List<SensorData> {
    val db = getDb()
    return db.from(Conditions)
            .select()
            .orderBy(Conditions.time.asc())
            .whereWithConditions {
                it += Conditions.time gt convert(from)
                if (to != null) {
                    it += (Conditions.time lt convert(to))
                }

                if (location != null) {
                    it += Conditions.location eq location
                }
            }
            .map { row ->
                SensorData(
                        location = row[Conditions.location]!!,
                        temperature_mc = row[Conditions.temperature_mc]!!,
                        humidity = row[Conditions.humidity]!!,
                        time =
                                Instant.fromEpochMilliseconds(
                                        row[Conditions.time]?.toEpochMilli() ?: 0
                                )
                )
            }
}
