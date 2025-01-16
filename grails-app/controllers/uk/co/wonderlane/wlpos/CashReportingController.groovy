package uk.co.wonderlane.wlpos


import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class CashReportingController {

    def index() {

    }

    def shiftFinalisation() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [startDate  : startDate, endDate    : endDate]
    }
}