package controllers;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sismics.sapparot.exception.ValidationException;
import play.Logger;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Util;
import play.mvc.results.RenderText;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author jtremeaux
 */
public class RestController extends Controller {
    public static final String ERROR_GLOBAL = "global";

    public static final String APPLICATION_JSON = "application/json";

    protected static void okJson() {
        JsonObject json = new JsonObject();
        json.addProperty("status", "ok");
        renderJSON(json);
    }

    protected static void renderId(UUID id) {
        JsonObject json = new JsonObject();
        JsonObject item = new JsonObject();
        item.addProperty("id", id.toString());
        json.add("item", item);
        renderJSON(json);
    }

    protected static void okJson(JsonObject json) {
        renderJSON(json.toString());
    }

    protected static void badRequestJson() {
        JPA.setRollbackOnly();
        response.status = Http.StatusCode.BAD_REQUEST;
        renderJSON(convertErrorMap(validation.errorsMap()));
    }

    protected static void forbiddenJson() {
        JPA.setRollbackOnly();
        response.status = Http.StatusCode.FORBIDDEN;
        renderJSON(convertErrorMap(validation.errorsMap()));
    }

    protected static void badRequestJsonIfHasError() {
        if (Validation.hasErrors()) {
            badRequestJson();
        }
    }

    /**
     * Normalize field names.
     * Ex. user[country][id] -> user.country.id
     *
     * @param errorMap
     * @return
     */
    protected static Map<String, List<Error>> convertErrorMap(Map<String, List<Error>> errorMap) {
        Pattern p = Pattern.compile("\\[.+\\]");
        Map<String, List<Error>> result = new HashMap<>();
        for (Map.Entry<String, List<Error>> error : errorMap.entrySet()) {
            String label = error.getKey().replaceAll("\\[(.+?)\\]", ".$1");
            result.put(label, error.getValue());
        }
        return result;
    }

    @Catch(value = ValidationException.class, priority = 1)
    public static void handleValidationException(ValidationException e) {
        validation.addError(e.getKey(), e.getMessage(), e.getArgs() != null ? e.getArgs() : new String[0]);

        Http.Header acceptHeader = request.headers.get("accept");
        boolean isJson = acceptHeader != null && acceptHeader.value().contains(APPLICATION_JSON);
        if (isJson) {
            badRequestJson();
        } else {
            badRequestPlainText();
        }
    }

    @Catch(value = RuntimeException.class, priority = 2)
    public static void handleUnexpectedException(RuntimeException e) {
        JPA.setRollbackOnly();
        response.status = Http.StatusCode.INTERNAL_ERROR;
        Logger.error(e, "Unexpected error");
        renderJSON(convertErrorMap(validation.errorsMap()));
    }

    /**
     * Throw a new Result with BAD_REQUEST as status and the validation errors as content *
     */
    @Util
    private static void badRequestPlainText() {
        JPA.setRollbackOnly();
        StringBuilder builder = new StringBuilder();
        for (List<Error> entry : validation.errorsMap().values()) {
            for (Error error : entry) {
                builder.append(error.message()).append("\n");
            }
        }
        throw new RenderText(builder.toString()) {
            public void apply(Http.Request request, Http.Response response) {
                response.status = Http.StatusCode.BAD_REQUEST;
                super.apply(request, response);
            }
        };
    }

    /**
     * Parse the contents of the JSON request body.
     *
     * @param clazz The class to convert to
     * @param <T> The type to convert to
     * @return The parsed object
     */
    protected static <T> T parseRequestBodyAsJson(Class<T> clazz) {
        return new Gson().fromJson(new InputStreamReader(Http.Request.current().body), clazz);
    }

    /**
     * Returns the contents of the request body as a string.
     *
     * @return The request body
     */
    protected static String getRequestBodyAsString() {
        try {
            return CharStreams.toString(new InputStreamReader(Http.Request.current().body, Charsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a single header frome the request.
     * Returns null if the header is not set
     *
     * @param key The header key
     * @return The header value
     */
    protected static String getRequestHeader(String key) {
        Http.Header header = request.headers.get(key);
        if (header == null) {
            return null;
        }
        return header.value();
    }
}
