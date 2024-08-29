package uk.co.wonderlane.wlpos.utils

import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class DateTimeUtils {

    private static final DateTimeFormatter FORMAT_HH_mm = DateTimeFormat.forPattern("HH:mm");

    static LocalTime timeFromFormatHHmm(String time) {
        return LocalTime.parse(time, FORMAT_HH_mm);
    }

    static String textTimeFromFormatHHmm(LocalTime localTime) {
        return localTime.toString(FORMAT_HH_mm)
    }
}
