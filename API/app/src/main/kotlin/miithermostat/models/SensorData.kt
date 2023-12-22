package miithermostat.models

import io.ktor.http.HttpStatusCode
import java.time.Instant as JavaInstant
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import miithermostat.getDb
import miithermostat.tools.convert
import org.ktorm.dsl.*
import org.ktorm.schema.*

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
        if (location == null) {
            return HttpStatusCode.BadRequest
        }
        val db = getDb()
        db.insert(Conditions) {
            set(it.device_id, device_id)
            set(it.temperature_mc, temperature_mc)
            set(it.humidity, humidity)
            set(it.location, location)
            if (time != null) {
                set(it.time, JavaInstant.ofEpochMilli(time.toEpochMilliseconds()))
            }
        }

        val offset = getDeviceOffset(device_id)
        if (offset != null) {
            val adjustedTemperatureMc: Short = (temperature_mc + offset.temperature_mc_offset).toShort()
            val adjustedHumidity: Short = (humidity + offset.humidity_offset).toShort()
            db.insert(AdjustedConditions) {
                set(it.device_id, device_id)
                set(it.temperature_mc, adjustedTemperatureMc)
                set(it.humidity, adjustedHumidity)
                set(it.location, location)
                if (time != null) {
                    set(it.time, JavaInstant.ofEpochMilli(time.toEpochMilliseconds()))
                }
            }   
        }
        return HttpStatusCode.Created
    }
}

data class DeviceOffset(
        val temperature_mc_offset: Short,
        val humidity_offset: Short,
)

object DevicesOffset : Table<Nothing>("devices_offset") {
    val device_id = varchar("device_id").primaryKey()
    val temperature_mc_offset = short("temperature_mc_offset")
    val humidity_offset = short("humidity_offset")
}

object AdjustedConditions : Table<Nothing>("sensor_data_with_offset") {
    val id = int("id").primaryKey()
    val device_id = varchar("device_id")
    val temperature_mc = short("temperature_mc")
    val humidity = short("humidity")
    val time = timestamp("time")
    val location = varchar("location")
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

fun getLatestSensorDataByLocation(): Map<String, SensorData> {
    val sensorMap: MutableMap<String, SensorData> = mutableMapOf()
    val db = getDb()
    if (db.productName == "SQLite") {
        val locations =
                db.from(Conditions).selectDistinct(Conditions.location).map { row ->
                    row[Conditions.location]!!
                }
        for (location: String in locations) {
            db.from(Conditions)
                    .select(Conditions.temperature_mc, Conditions.humidity, Conditions.time)
                    .orderBy(Conditions.time.desc())
                    .where { Conditions.location eq location }
                    .limit(1)
                    .map { row ->
                        val temperature_mc = row[Conditions.temperature_mc]!!
                        val humidity = row[Conditions.humidity]!!
                        val time =
                                Instant.fromEpochMilliseconds(
                                        row[Conditions.time]?.toEpochMilli() ?: 0
                                )
                        sensorMap.put(
                                location,
                                SensorData(
                                        temperature_mc = temperature_mc,
                                        humidity = humidity,
                                        time = time
                                )
                        )
                    }
        }
    } else {
        db.useConnection { conn ->
            val sql =
                    """
            select distinct on (location) location, temperature_mc, humidity, time 
            from sensor_data 
            order by location, time desc;
            """
            val resultSet = conn.prepareStatement(sql).executeQuery()
            while (resultSet.next()) {
                val location = resultSet.getString("location")!!
                val temperature_mc = resultSet.getShort("temperature_mc")
                val humidity = resultSet.getShort("humidity")
                val time =
                        Instant.fromEpochMilliseconds(
                                resultSet.getTimestamp("time")?.toInstant()?.toEpochMilli() ?: 0
                        )
                sensorMap.put(
                        location,
                        SensorData(
                                temperature_mc = temperature_mc,
                                humidity = humidity,
                                time = time
                        )
                )
            }
        }
    }
    return sensorMap
}

fun getDeviceOffset(deviceId: String): DeviceOffset? {
    val db = getDb()
    return db.from(DevicesOffset)
    .select()
    .where { DevicesOffset.device_id eq deviceId }
    .limit(1)
    .map { row
        ->
        DeviceOffset(
                temperature_mc_offset = row[DevicesOffset.temperature_mc_offset]!!,
                humidity_offset = row[DevicesOffset.humidity_offset]!!
        )
    }.firstOrNull()
}
