package uk.co.wonderlane.wlpos

import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.ISODateTimeFormat
import uk.co.wonderlane.wlpos.adapters.LocalDateTypeAdapter
import uk.co.wonderlane.wlpos.adapters.LocalTimeTypeAdapter
import uk.co.wonderlane.wlpos.entities.basketv2.BasketItem
import uk.co.wonderlane.wlpos.entities.event.message.TaskEventMessage
import uk.co.wonderlane.wlpos.entities.transactionv2.Transaction
import uk.co.wonderlane.wlpos.requests.clientexport.StockTransaction
import uk.co.wonderlane.wlpos.utils.GsonUtil
import uk.co.wonderlane.wlpos.utils.PropertyBasedInterfaceMarshal

import java.lang.reflect.Type
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class GsonProvider {

    private Gson gson

    GsonProvider() {
        // todo - shifts are using a different date format so may need to use their own gson in the future
        gson = GsonUtil.getGson("yyyy-MM-dd")
    }

    def getGson() {
        return gson
    }
}