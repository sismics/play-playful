package helpers.locale;

import java.util.Locale;

/**
 * Locale utilities.
 *
 * @author jtremeaux
 */
public class LocaleUtil {
    /**
     * Return the locale from it's code language / country / variant (e.g. fr_FR).
     * 
     * @param localeCode The locale code
     * @return The java Locale
     */
    public static final Locale getLocale(String localeCode) {
        String[] localeCodeArray = localeCode.split("_");
        String language = localeCodeArray[0];
        String country = "";
        String variant = "";
        if (localeCodeArray.length >= 2) {
            country = localeCodeArray[1];
        }
        if (localeCodeArray.length >= 3) {
            variant = localeCodeArray[2];
        }
        return new Locale(language, country, variant);
    }

    /**
     * Get the country name from the country code.
     *
     * @param code The country code
     * @param locale In which locale to print the country name
     * @return The country name
     */
    public static String getCountryName(String code, Locale locale) {
        return new Locale("", code, "").getDisplayCountry(locale);
    }
}
