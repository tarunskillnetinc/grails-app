package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class ShelfEdgeLabelController {

    def index() {
        def dateTime = DateTime.now();

        def dates = [dateTime.toString("dd/MM/yyyy"), dateTime.minusDays(1).toString("dd/MM/YYYY"), dateTime.minusDays(2).toString("dd/MM/yyyy")]

        [dates: dates]
    }
}
