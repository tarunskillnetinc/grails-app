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
import uk.co.wonderlane.wlpos.utils.PropertyBasedInterfaceMarshal

import java.lang.reflect.Type
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class GsonProvider {

    private Gson gson

    GsonProvider() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
                    @Override
                    JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json))
                    }
                })
                .registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
                    @Override
                    DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()).withZone(DateTimeZone.UTC)
                    }
                })
                .registerTypeAdapter(StockTransaction.class, new PropertyBasedInterfaceMarshal())
                .registerTypeAdapter(TaskEventMessage.class, new PropertyBasedInterfaceMarshal())
                .registerTypeAdapter(uk.co.wonderlane.wlpos.entities.basket.BasketItem.class, new PropertyBasedInterfaceMarshal())
                .registerTypeAdapter(uk.co.wonderlane.wlpos.entities.transaction.Transaction.class, new PropertyBasedInterfaceMarshal())
                .registerTypeAdapter(BasketItem.class, new PropertyBasedInterfaceMarshal())
                .registerTypeAdapter(Transaction.class, new PropertyBasedInterfaceMarshal())
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
                .registerTypeAdapter(java.time.LocalDateTime.class, (JsonSerializer<java.time.LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(src.atZone(ZoneOffset.UTC).toInstant())))
                .registerTypeAdapter(java.time.LocalDateTime.class, (JsonDeserializer<java.time.LocalDateTime>) (json, typeOfT, context) -> java.time.LocalDateTime.ofInstant(java.time.Instant.parse(json.getAsString()), ZoneOffset.UTC))
                .registerTypeAdapter(java.time.LocalDate.class, (JsonSerializer<java.time.LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE)))
                .registerTypeAdapter(java.time.LocalDate.class, (JsonDeserializer<java.time.LocalDate>) (json, typeOfT, context) -> java.time.LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_DATE))
                .registerTypeAdapter(java.time.LocalTime.class, (JsonSerializer<java.time.LocalTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_TIME)))
                .registerTypeAdapter(java.time.LocalTime.class, (JsonDeserializer<java.time.LocalTime>) (json, typeOfT, context) -> java.time.LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_TIME))
                .create()
    }

    def getGson() {
        return gson
    }
}