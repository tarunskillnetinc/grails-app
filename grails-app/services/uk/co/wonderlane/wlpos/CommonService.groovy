package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.sql.Date
import java.sql.Timestamp

class CommonService {

    public static String DATE_PATTERN_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss"

    Date convertToSqlDate(DateTime date) {
        return new Date(date.withTimeAtStartOfDay().getMillis());
    }

    String convertDateTimeToString(DateTime dateTime) {
        if(dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_PATTERN_YYYYMMDD_HHMMSS);
        return dateTime.toString(formatter);
    }

    Timestamp convertToSqlTimestamp(DateTime dateTime) {
        if (dateTime != null){
            return new Timestamp(dateTime.getMillis());
        }
        return new Timestamp(new DateTime().getMillis());
    }
}
