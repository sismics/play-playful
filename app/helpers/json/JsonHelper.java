package helpers.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import helpers.extension.PlayfulJavaExtensions;
import play.mvc.Util;

/**
 * @author jtremeaux
 */
public class JsonHelper {
    @Util
    public static JsonObject getAsJsonObject(Object object) {
        Gson gson = new GsonBuilder()
                .setDateFormat(PlayfulJavaExtensions.ISO8601)
                .create();

        return (JsonObject) gson.toJsonTree(object);
    }
}
