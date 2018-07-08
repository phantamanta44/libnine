package io.github.phantamanta44.libnine.util.helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonUtils9 {

    public static final Gson GSON = new Gson();
    public static final JsonParser PARSER = new JsonParser();

    public static Stream<JsonElement> stream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }

    private static MirrorUtils.IMethod<? extends JsonElement> mDeepCopy
            = MirrorUtils.reflectMethod(JsonElement.class, "deepCopy");

    @SuppressWarnings("unchecked")
    public static <T extends JsonElement> T copy(T o) {
        return (T)mDeepCopy.invoke(o);
    }

}
