package miithermostat.tools

import java.time.Duration
import java.time.Instant
import kotlinx.datetime.Instant as InstantKt
import kotlin.time.toKotlinDuration

fun convert(instant: kotlinx.datetime.Instant): Instant {
    return Instant.ofEpochSecond(instant.epochSeconds)
}

fun prettyDate(time: InstantKt): String {
    /*
    Get a datetime object or a int() Epoch timestamp and return a
    pretty string like 'an hour ago', 'Yesterday', '3 months ago',
    'just now', etc
    */
    val timeBetween = Duration.between(Instant.ofEpochMilli(time.toEpochMilliseconds()), Instant.now()).toKotlinDuration();
    val second_diff = timeBetween.inWholeSeconds;
    val day_diff = timeBetween.inWholeDays;

    if (day_diff < 0) {
        return "";
    }

    if (day_diff == 0L) {
        if (second_diff < 10) {
            return "just now";
        }
        if (second_diff < 60) {
            return String.format("%s seconds ago", second_diff);
        }
        if (second_diff < 120) {
            return "a minute ago";
        }
        if (second_diff < 3600) {
            return String.format("%s minutes ago", second_diff / 60);
        }
        if (second_diff < 7200) {
            return "an hour ago";
        }
        if (second_diff < 86400) {
            return String.format("%s hours ago", second_diff / 3600);
        }
    }
    if (day_diff == 1L) {
        return "Yesterday";
    }
    if (day_diff < 7) {
        return String.format("%s days ago", day_diff);
    }
    if (day_diff < 31) {
        return String.format("%s weeks ago", day_diff / 7);
    }
    if (day_diff < 365) {
        return String.format("%s months ago", day_diff / 30);
    }
    return String.format("%s years ago", day_diff / 365);
}
