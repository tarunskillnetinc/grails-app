package uk.co.wonderlane.wlpos.usertypes

import com.google.gson.*

import java.lang.reflect.Type

class BooleanTypeAdapter implements JsonDeserializer<Boolean> {

    @Override
    Boolean deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (((JsonPrimitive) jsonElement).isBoolean()) {
            return jsonElement.getAsBoolean()
        }

        if (((JsonPrimitive) jsonElement).isString()) {
            String jsonValue = jsonElement.getAsString()
            if (jsonValue.equalsIgnoreCase("true")) {
                return true
            } else if (jsonValue.equalsIgnoreCase("false")) {
                return false
            } else {
                return null
            }
        }

        int code = jsonElement.getAsInt()

        return code == 0 ? false : code == 1 ? true : null
    }
}
