package miithermostat.models

import io.ktor.http.HttpStatusCode
import java.time.Instant as JavaInstant
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import miithermostat.getDb
import miithermostat.tools.convert
import org.postgresql.util.PSQLException
import org.ktorm.dsl.*
import org.ktorm.schema.*

@Serializable
data class SensorData(
        val device_id: String? = null,
        val temperature_mc: Short,
        val humidity_pt: Short,
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
        db.insert(RawConditions) {
            set(it.device_id, device_id)
            set(it.temperature_mc, temperature_mc)
            set(it.humidity_pt, humidity_pt)
            set(it.location, location)
            if (time != null) {
                set(it.time, JavaInstant.ofEpochMilli(time.toEpochMilliseconds()))
            }
        }

        val offset = getDeviceOffset(device_id)
        if (offset != null) {
            val adjustedTemperatureMc: Short = (temperature_mc + offset.temperature_mc_offset).toShort()
            val adjustedHumidityPt: Short = (humidity_pt + offset.humidity_pt_offset).toShort()
            db.insert(Conditions) {
                set(it.device_id, device_id)
                set(it.temperature_mc, adjustedTemperatureMc)
                set(it.humidity_pt, adjustedHumidityPt)
                set(it.location, location)
                if (time != null) {
                    set(it.time, JavaInstant.ofEpochMilli(time.toEpochMilliseconds()))
                }
            }   
        }
        return HttpStatusCode.Created
    }
}

@Serializable
data class DeviceOffset(
        val temperature_mc_offset: Short,
        val humidity_pt_offset: Short,
)

object DevicesOffset : Table<Nothing>("devices_offset") {
    val device_id = varchar("device_id").primaryKey()
    val temperature_mc_offset = short("temperature_mc_offset")
    val humidity_pt_offset = short("humidity_pt_offset")
}

object Conditions : Table<Nothing>("sensor_data_with_offset") {
    val id = int("id").primaryKey()
    val device_id = varchar("device_id")
    val temperature_mc = short("temperature_mc")
    val humidity_pt = short("humidity_pt")
    val time = timestamp("time")
    val location = varchar("location")
}

object RawConditions : Table<Nothing>("sensor_data") {
    val id = int("id").primaryKey()
    val device_id = varchar("device_id")
    val temperature_mc = short("temperature_mc")
    val humidity_pt = short("humidity_pt")
    val time = timestamp("time")
    val location = varchar("location")
}

fun getAllSensorData(): List<SensorData> {
    val db = getDb()
    return db.from(Conditions).select().orderBy(Conditions.time.asc()).map { row ->
        SensorData(
                device_id = row[Conditions.device_id]!!,
                location = row[Conditions.location]!!,
                temperature_mc = row[Conditions.temperature_mc]!!,
                humidity_pt = row[Conditions.humidity_pt]!!,
                time = Instant.fromEpochMilliseconds(row[Conditions.time]?.toEpochMilli() ?: 0)
        )
    }
}

fun getSensorData(from: Instant, to: Instant? = null, location: String? = null): List<SensorData> {
    val db = getDb()
    return db.from(Conditions)
            .select()
            .orderBy(Conditions.time.asc(), Conditions.location.asc(), Conditions.device_id.asc())
            .whereWithConditions {
                it += Conditions.time gt convert(from)
                if (to != null) {
                    it += (Conditions.time lte convert(to))
                }

                if (location != null) {
                    it += Conditions.location eq location
                }
            }
            .map { row ->
                SensorData(
                        device_id = row[Conditions.device_id]!!,
                        location = row[Conditions.location]!!,
                        temperature_mc = row[Conditions.temperature_mc]!!,
                        humidity_pt = row[Conditions.humidity_pt]!!,
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
    db.useConnection { conn ->
            val sql =
                    """
            select distinct on (location) location, temperature_mc, humidity_pt, time 
            from sensor_data_with_offset 
            order by location, time desc;
            """
            val resultSet = conn.prepareStatement(sql).executeQuery()
            while (resultSet.next()) {
                val location = resultSet.getString("location")!!
                val temperature_mc = resultSet.getShort("temperature_mc")
                val humidity_pt = resultSet.getShort("humidity_pt")
                val time =
                        Instant.fromEpochMilliseconds(
                                resultSet.getTimestamp("time")?.toInstant()?.toEpochMilli() ?: 0
                        )
                sensorMap.put(
                        location,
                        SensorData(
                                temperature_mc = temperature_mc,
                                humidity_pt = humidity_pt,
                                time = time
                        )
                )
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
                humidity_pt_offset = row[DevicesOffset.humidity_pt_offset]!!
        )
    }.firstOrNull()
}

fun setDeviceOffset(deviceId: String, deviceOffset: DeviceOffset): HttpStatusCode {
    return setDeviceOffset(deviceId, deviceOffset.temperature_mc_offset, deviceOffset.humidity_pt_offset)
}

fun setDeviceOffset(deviceId: String, temperatureMcOffset: Short, humidityPtOffset: Short): HttpStatusCode {
    val db = getDb()
    try {
        db.insert(DevicesOffset) {
            set(it.device_id, deviceId)
            set(it.temperature_mc_offset, temperatureMcOffset)
            set(it.humidity_pt_offset, humidityPtOffset)
        }
        return HttpStatusCode.Created
    } catch (e: PSQLException) {
        // Ignored as InsertOrUpdate not super supported.
    }
    try {
        val updatedCount = db.update(DevicesOffset) {
            set(it.temperature_mc_offset, temperatureMcOffset)
            set(it.humidity_pt_offset, humidityPtOffset)
            where {
                it.device_id eq deviceId
            }
        }
        if (updatedCount == 1) {
            return HttpStatusCode.OK
        }
    } catch (e: PSQLException) {
        // Ignored as InsertOrUpdate not super supported.
    }
    return HttpStatusCode.NotFound
}