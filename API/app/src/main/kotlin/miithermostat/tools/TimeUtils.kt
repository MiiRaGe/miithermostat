package miithermostat.tools

import java.time.Instant

fun convert(instant: kotlinx.datetime.Instant): Instant {
    return Instant.ofEpochSecond(instant.epochSeconds)
}