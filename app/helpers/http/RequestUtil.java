package helpers.http;

import play.mvc.Http;

import java.util.List;

/**
 * @author jtremeaux
 */
public class RequestUtil {
    public static String getFirstAcceptLanguage() {
        List<String> acceptLanguage = Http.Request.current().acceptLanguage();
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return null;
        }
        return acceptLanguage.iterator().next();
    }
}
