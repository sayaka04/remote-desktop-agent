package remoteagent.utils;

import com.google.gson.*;

public class Json {

    private static final Gson gson = new Gson();

    // Object → JSON string
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    // JSON string → Object
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    // JSON string → JsonObject (dynamic access)
    public static JsonObject parseObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    // Get safe JsonObject field
    public static JsonObject getObject(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonObject() ? el.getAsJsonObject() : null;
    }

    // Get safe JsonArray field
    public static JsonArray getArray(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonArray() ? el.getAsJsonArray() : null;
    }

    // Get safe string field
    public static String getString(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && !el.isJsonNull() ? el.getAsString() : null;
    }

    // Get safe integer field
    public static int getInt(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null ? el.getAsInt() : 0;
    }

    // Get safe boolean field
    public static boolean getBool(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.getAsBoolean();
    }
}