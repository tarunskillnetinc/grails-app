package uk.co.wonderlane.wlpos

import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import uk.co.wonderlane.wlpos.requests.clientexport.StockTransaction
import uk.co.wonderlane.wlpos.utils.PropertyBasedInterfaceMarshal

import java.lang.reflect.Type

class GsonProvider {

    private Gson gson

    GsonProvider() {
        gson = new GsonBuilder()
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
                .create()
    }

    def getGson() {
        return gson
    }
}