package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

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
                .registerTypeAdapter(Boolean.class, new JsonDeserializer<Boolean>() {
                    @Override
                    Boolean deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        if (((JsonPrimitive) jsonElement).isBoolean()) {
                            return jsonElement.getAsBoolean();
                        }

                        if (((JsonPrimitive) jsonElement).isString()) {
                            String jsonValue = jsonElement.getAsString();
                            if (jsonValue.equalsIgnoreCase("true")) {
                                return true;
                            } else if (jsonValue.equalsIgnoreCase("false")) {
                                return false;
                            } else {
                                return null;
                            }
                        }

                        int code = jsonElement.getAsInt();

                        return code == 0 ? false : code == 1 ? true : null;
                    }
                })
                .serializeNulls()
                .create()
    }

    def getGson() {
        return gson
    }
}