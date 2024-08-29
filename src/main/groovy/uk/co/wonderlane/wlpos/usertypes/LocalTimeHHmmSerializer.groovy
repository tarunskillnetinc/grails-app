package uk.co.wonderlane.wlpos.usertypes

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.lang.reflect.Type

class LocalTimeHHmmSerializer implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    // Formatter for HH:mm format
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm")

    @Override
    JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString(formatter))
    }

    @Override
    LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return formatter.parseLocalTime(json.asString)
    }
}
