package io.github.phantamanta44.libnine.util.helper;

import com.google.gson.*;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonUtils9 {

    public static final Gson GSON = new Gson();
    public static final JsonParser PARSER = new JsonParser();

    public static Stream<JsonElement> stream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }

    private static final MirrorUtils.IMethod<? extends JsonElement> mDeepCopy
            = MirrorUtils.reflectMethod(JsonElement.class, "deepCopy");

    @SuppressWarnings("unchecked")
    public static <T extends JsonElement> T copy(T o) {
        return (T)mDeepCopy.invoke(o);
    }

    public static void merge(JsonObject base, JsonObject merger) {
        for (Map.Entry<String, JsonElement> entry : merger.entrySet()) {
            JsonElement baseElem = base.get(entry.getKey());
            if (entry.getValue().isJsonObject()) {
                if (baseElem != null && baseElem.isJsonObject()) {
                    merge(baseElem.getAsJsonObject(), entry.getValue().getAsJsonObject());
                } else {
                    base.add(entry.getKey(), copy(entry.getValue()));
                }
            } else {
                base.add(entry.getKey(), copy(entry.getValue()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends JsonElement> T mergeCloning(T base, T merger) {
        if (base.isJsonObject()) {
            JsonObject result = copy(base.getAsJsonObject());
            merge(result, (JsonObject)merger);
            return (T)result;
        }
        return copy(merger);
    }

}
